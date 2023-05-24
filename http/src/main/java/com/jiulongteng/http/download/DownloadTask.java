package com.jiulongteng.http.download;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.download.dispatcher.CallbackDispatcher;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;
import com.jiulongteng.http.util.CollectionUtils;
import com.jiulongteng.http.util.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 单个下载任务控制类
 */
public class DownloadTask {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            Util.threadFactory("OkDownload Block", false));

    final private AtomicBoolean isStopped;
    OkHttpClient client;
    Request rawRequest;
    String fileName;
    @Nullable
    private String redirectLocation;


    private boolean acceptRange;
    @IntRange(from = Util.CHUNKED_CONTENT_LENGTH)
    private long instanceLength;
    @Nullable
    private String responseEtag;
    @Nullable
    private String responseFilename;
    private int responseCode;
    private BreakpointInfo info;
    private boolean isFilenameFromResponse;
    DownloadPretreatment pretreatment;
    DownloadPostTreatment postTreatment;

    @Nullable
    private final Integer connectionCount;
    ArrayList<AbstractDownloadRunnable> downloadRunnables;

    FlushRunnable flushRunnable;

    Runnable finishRunnable;
    Throwable causeThrowable;

    DownloadListener downloadListener;
    SpeedListener speedListener;
    CallbackDispatcher callbackDispatcher;
    AtomicInteger taskStatus = new AtomicInteger(DownloadCache.PENDING);
    private TargetProvider targetProvider;
    private Headers responseHeaders;

    private String md5Code;

    public DownloadTask(File file, OkHttpClient client, Request request, Integer connectionCount) {
        this(new FileTargetProvider(file), client, request, connectionCount);
    }

    public DownloadTask(TargetProvider targetProvider, OkHttpClient client, Request request, Integer connectionCount) {
        this.targetProvider = targetProvider;
        if (TextUtils.isEmpty(targetProvider.getFileName())) {
            this.isFilenameFromResponse = true;
        } else {
            this.fileName = targetProvider.getFileName();
        }

        this.client = client;
        this.rawRequest = request;
        this.isStopped = new AtomicBoolean(false);
        this.connectionCount = connectionCount;
        this.callbackDispatcher = new CallbackDispatcher(DownloadCache.getInstance().isAndroid() ? new Handler(Looper.getMainLooper()) : null);

    }

    public DownloadTask(File file, OkHttpClient client, Request request) {
        this(file, client, request, null);
    }

    private void reset() {
        setIsStopped(false);
        causeThrowable = null;
    }

    /**
     * 下载前预处理
     * 启动多线程下载
     */
    public void execute() {
        reset();
        getCallbackDispatcher().taskStart(this);
        pretreatment = new DownloadPretreatment(this);
        try {
            pretreatment.execute();
        } catch (Throwable e) {
            dispatchError(e);
            return;
        }
        DownloadCache.getInstance().updateDownloadInfo(info);
        if (isStopped.get()) {
            dispatchCancel();
            return;
        }

        if (DownloadCache.getInstance().isFileConflictAfterRun(this)) {
            getCallbackDispatcher().taskEnd(this, EndCause.SAME_FILE_BUSY, null);
            return;
        }

        ArrayList<AbstractDownloadRunnable> runnables = prepareSubTask();
        downloadRunnables = new ArrayList<>();
        downloadRunnables.addAll(runnables);
        flushRunnable = new FlushRunnable(this, finishRunnable);

        getCallbackDispatcher().fetchStart(this, getInfo().getTotalOffset() == 0);
        try {
            startSubTask(runnables);
        } catch (InterruptedException e) {

        }
        if (getInfo().getTotalOffset() == getInfo().getTotalLength()) {
            dispatchComplete();
        } else {
            if (causeThrowable != null) {
                dispatchError(causeThrowable);
            } else {
                dispatchCancel();
            }
        }
    }

    /**
     * 添加进下载队列
     * 按添加顺序启动下载
     */
    public void enqueue() {
        DownloadCache.getInstance().enqueueTask(this);
    }

    /**
     * 对暂停或者结束的任务重新开始加入队列下载
     *
     * @return
     */
    public boolean reStart() {
        int taskStatus = getTaskStatus();
        if (taskStatus == DownloadCache.STOP) {
            return DownloadCache.getInstance().submitTask(this);
        }
        return false;
    }

    /**
     * 运行在磁盘io结束的任务
     *
     * @param runnable
     */
    public void setFinishRunnable(Runnable runnable) {
        this.finishRunnable = runnable;
    }

    /**
     * 准备多线程下载需要信息
     *
     * @return
     */
    private ArrayList<AbstractDownloadRunnable> prepareSubTask() {

        final int blockCount = info.getBlockCount();
        ArrayList<AbstractDownloadRunnable> runnables = new ArrayList<>();
        int index = 0;
        for (int i = 0; i < blockCount; i++) {
            final BlockInfo blockInfo = info.getBlock(i);
            if (Util.isCorrectFull(blockInfo.getCurrentOffset(), blockInfo.getContentLength())) {
                continue;
            }
            Util.resetBlockIfDirty(blockInfo);
            if (DownloadCache.getInstance().isAndroid()) {
                runnables.add(new DownloadAndroidRunnable(this, blockInfo, index));
            } else {
                runnables.add(new DownloadRunnable(this, blockInfo, index));
            }
            index++;

        }
        return runnables;

    }

    /**
     * 启动多线程下载任务
     *
     * @param runnables
     * @throws InterruptedException
     */
    private void startSubTask(ArrayList<AbstractDownloadRunnable> runnables) throws InterruptedException {
        if (runnables.isEmpty()) {
            return;
        }
        ArrayList<Future> futures = new ArrayList<>();
        try {
            for (Runnable runnable : runnables) {
                futures.add(EXECUTOR.submit(runnable));
            }
            for (Future future : futures) {
                if (!future.isDone()) {
                    try {
                        future.get();
                    } catch (CancellationException | ExecutionException ignore) {
                    }
                }
            }
        } catch (Throwable t) {
            for (Future future : futures) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
            }
            throw t;
        } finally {
        }
    }


//    public File getParentFile() {
//        return targetProvider.getParentFile();
//    }

    public String getUrl() {
        return rawRequest.url().toString();
    }

    public String getFilename() {
        return fileName;
    }

    public boolean isFilenameFromResponse() {
        return isFilenameFromResponse;
    }

//    public File getFile() {
//        return targetProvider.getTargetFile();
//    }


    public void setIsStopped(boolean isStopped) {
        this.isStopped.set(isStopped);
    }


    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = client;
    }

    public Request getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(Request rawRequest) {
        this.rawRequest = rawRequest;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        targetProvider.setFileName(fileName);

    }


    @Nullable
    public String getRedirectLocation() {
        return redirectLocation;
    }

    public void setRedirectLocation(@Nullable String redirectLocation) {
        this.redirectLocation = redirectLocation;
    }

    public boolean isAcceptRange() {
        return acceptRange;
    }

    public void setAcceptRange(boolean acceptRange) {
        this.acceptRange = acceptRange;
    }

    public long getInstanceLength() {
        return instanceLength;
    }

    public void setInstanceLength(long instanceLength) {
        this.instanceLength = instanceLength;
    }

    @Nullable
    public String getResponseEtag() {
        return responseEtag;
    }

    public void setResponseEtag(@Nullable String responseEtag) {
        this.responseEtag = responseEtag;

    }

    @Nullable
    public String getResponseFilename() {
        return responseFilename;
    }

    public void setResponseFilename(@Nullable String responseFilename) {
        this.responseFilename = responseFilename;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public BreakpointInfo getInfo() {
        return info;
    }

    public void setInfo(BreakpointInfo info) {
        this.info = info;
    }


    @Nullable
    public Integer getConnectionCount() {
        return connectionCount;
    }

    public boolean isStoped() {
        return isStopped.get();
    }

    public void dispatchCancel() {
        DownloadCache.getInstance().onStop(this, true);
        getCallbackDispatcher().taskEnd(this, EndCause.CANCELED, null);
        dispatchPostTreatment(this, EndCause.CANCELED, null);
    }

    public void dispatchError(Throwable throwable) {
        DownloadCache.getInstance().onStop(this, true);
        getCallbackDispatcher().taskEnd(this, EndCause.ERROR, throwable);
        dispatchPostTreatment(this, EndCause.ERROR, throwable);
    }

    public void dispatchComplete() {
        DownloadCache.getInstance().deleteInfo(info.getId());
        DownloadCache.getInstance().onComplete(this, true);
        getCallbackDispatcher().taskEnd(this, EndCause.COMPLETED, null);
        dispatchPostTreatment(this, EndCause.COMPLETED, null);
    }

    public int getRunnableSize() {
        if (downloadRunnables == null) {
            return 0;
        }
        return downloadRunnables.size();
    }

    public ArrayList<AbstractDownloadRunnable> getDownloadRunnables() {
        return downloadRunnables;
    }

    public FlushRunnable getFlushRunnable() {
        return flushRunnable;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(DownloadListener listener) {
        this.downloadListener = listener;

    }

    public void setSpeedListener(SpeedListener listener) {
        speedListener = listener;
    }

    public SpeedListener getSpeedListener() {
        return speedListener;
    }

    public CallbackDispatcher getCallbackDispatcher() {
        return callbackDispatcher;
    }

    public void setStatus(int pretreatment) {
        taskStatus.set(pretreatment);
    }

    public int getTaskStatus() {
        return taskStatus.get();
    }


    /**
     * 暂停下载
     */
    public void stop() {
        if (taskStatus.compareAndSet(DownloadCache.RUNNING, DownloadCache.STOP)) {
            setIsStopped(true);
            if (!CollectionUtils.isEmpty(getDownloadRunnables())) {
                for (AbstractDownloadRunnable runnable : getDownloadRunnables()) {
                    runnable.setIsReadByteFinished(true);
                    runnable.interrupt();
                    getFlushRunnable().done(runnable);

                }
            }
        } else {
            throw new IllegalStateException(" task allready stoped");
        }


    }

    public void setPostTreatment(DownloadPostTreatment postTreatment) {
        this.postTreatment = postTreatment;
    }

    public void dispatchPostTreatment(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Throwable realCause) {
        if (postTreatment != null) {
            postTreatment.onEnd(task, cause, realCause);
            postTreatment = null;
        }
    }

    public void setThrowable(Throwable e) {
        this.causeThrowable = e;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadTask that = (DownloadTask) o;
        if (that.getInfo() != null && this.getInfo() != null && that.getInfo().getId() == this.getInfo().getId()) {
            return true;
        }
        return rawRequest.url().toString().equals(that.rawRequest.url().toString()) &&
                Objects.equals(fileName, that.fileName) &&
                targetProvider.getParentFile().equals(that.getTargetProvider().getParentFile());
    }

    public TargetProvider getTargetProvider() {
        return this.targetProvider;
    }

    public void setResponseHeaders(Headers responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Headers getResponseHeaders() {
        return responseHeaders;
    }

    public String getMd5Code() {
        return md5Code;
    }

    public void setMd5Code(String md5Code) {
        this.md5Code = md5Code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawRequest.url().toString(), fileName, getTargetProvider().getParentFile().getAbsolutePath());
    }
}
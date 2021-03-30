package com.jiulongteng.http.download;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.download.dispatcher.CallbackDispatcher;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;
import com.jiulongteng.http.util.CollectionUtils;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DownloadTask {

    private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            Util.threadFactory("OkDownload Block", false));
    private File mFile;

    final private AtomicBoolean isStopped;
    OkHttpClient client;
    Request rawRequest;
    String fileName;
    File parentFile;
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

    @Nullable
    private final Integer connectionCount;
    ArrayList<AbstractDownloadRunnable> downloadRunnables;

    FlushRunnable flushRunnable;

    Runnable finishRunnable;
    Throwable causeThrowable;

    DownloadListener downloadListener;
    CallbackDispatcher callbackDispatcher;
    AtomicInteger taskStatus = new AtomicInteger(DownloadCache.PENDING);

    public DownloadTask(File file, OkHttpClient client, Request request, Integer connectionCount) {

        if (file.isDirectory()) {
            this.parentFile = file;
            this.isFilenameFromResponse = true;
        } else {
            this.mFile = file;
            this.parentFile = file.getParentFile();
            this.fileName = file.getName();
        }

        this.client = client;
        this.rawRequest = request;
        this.isStopped = new AtomicBoolean(false);
        this.connectionCount = connectionCount;
    }

    private void reset() {
        setIsStopped(false);
        causeThrowable = null;
    }

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

        if(DownloadCache.getInstance().isFileConflictAfterRun(this)){
            getCallbackDispatcher().taskEnd(this,EndCause.SAME_FILE_BUSY,null);
            return;
        }

        ArrayList<AbstractDownloadRunnable> runnables = prepareSubTask();
        downloadRunnables = new ArrayList<>();
        downloadRunnables.addAll(runnables);
        flushRunnable = new FlushRunnable(this, finishRunnable);

        getCallbackDispatcher().fetchStart(this, getInfo().getTotalOffset() > 0);
        try {
            startSubTask(runnables);
        } catch (InterruptedException e) {

        }
        if (getInfo().getTotalOffset() == getInfo().getTotalLength()) {
            DownloadCache.getInstance().deleteInfo(info.getId());
            DownloadCache.getInstance().onComplete(this, true);
            getCallbackDispatcher().taskEnd(this, EndCause.COMPLETED, null);
        } else {
            if (causeThrowable != null) {
                dispatchError(causeThrowable);
            } else {
                dispatchCancel();
            }

        }


    }

    public void enqueue() {
        DownloadCache.getInstance().enqueueTask(this);
    }

    public boolean reStart(boolean enqueueWhenFull) {
        int taskStatus = getTaskStatus();
        if (taskStatus != DownloadCache.RUNNING) {
            return DownloadCache.getInstance().submitTask(this, enqueueWhenFull);
        }
        return false;
    }

    public void setFinishRunnable(Runnable runnable) {
        this.finishRunnable = runnable;
    }

    private ArrayList<AbstractDownloadRunnable> prepareSubTask() {

        final int blockCount = info.getBlockCount();
        ArrayList<AbstractDownloadRunnable> runnables = new ArrayList<>();
        for (int i = 0; i < blockCount; i++) {
            final BlockInfo blockInfo = info.getBlock(i);
            if (Util.isCorrectFull(blockInfo.getCurrentOffset(), blockInfo.getContentLength())) {
                continue;
            }
            Util.resetBlockIfDirty(blockInfo);
            runnables.add(new DownloadRunnable(this, blockInfo, i));
        }
        return runnables;

    }

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


    public File getParentFile() {
        return parentFile;
    }

    public String getUrl() {
        return rawRequest.url().toString();
    }

    public String getFilename() {
        return fileName;
    }

    public boolean isFilenameFromResponse() {
        return isFilenameFromResponse;
    }

    public File getFile() {
        return mFile;
    }


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
        this.mFile = new File(parentFile, fileName);
    }

    public void setParentFile(File parentFile) {
        this.parentFile = parentFile;
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
        DownloadCache.getInstance().onStop(this, false);
        getCallbackDispatcher().taskEnd(this, EndCause.CANCELED, null);
    }

    public void dispatchError(Throwable throwable) {
        DownloadCache.getInstance().onStop(this, false);
        getCallbackDispatcher().taskEnd(this, EndCause.ERROR, throwable);
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

    public void setDownloadListener(boolean uiThread, DownloadListener listener) {
        this.downloadListener = listener;
        callbackDispatcher = new CallbackDispatcher(uiThread ? new Handler(Looper.getMainLooper()) : null);
    }

    public void setDownloadListener(DownloadListener listener) {
        setDownloadListener(true, listener);
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



    public void setThrowable(Exception e) {
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
                parentFile.equals(that.parentFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawRequest.url().toString(), fileName, parentFile.getAbsolutePath());
    }
}
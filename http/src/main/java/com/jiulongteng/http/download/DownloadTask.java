package com.jiulongteng.http.download;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.Dao;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.download.dispatcher.CallbackDispatcher;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
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

    private AtomicBoolean isStoped;
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
    CopyOnWriteArrayList<AbstractDownloadRunnable> downloadRunnables;

    FlushRunnable flushRunnable;

    Runnable finishRunnable;
    Throwable causeThrowable;

    DownloadListener downloadListener;
    CallbackDispatcher callbackDispatcher;
    AtomicInteger taskStatus =new AtomicInteger(DownloadCache.PENDING);
    AtomicBoolean isSuccess = new AtomicBoolean(false);
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
        this.isStoped = new AtomicBoolean(false);
        this.connectionCount = connectionCount;
    }


    public void execute() {
        getCallbackDispatcher().taskStart(this);
        pretreatment = new DownloadPretreatment(this);
        try {
            pretreatment.execute();
        } catch (Throwable e) {
            getCallbackDispatcher().taskEnd(this, EndCause.ERROR,e);
            return;
        }
        DownloadCache.getInstance().updateDownloadInfo(info);
        if (isStoped.get()) {
            return;
        }
        ArrayList<AbstractDownloadRunnable> runnables = prepareSubTask();
        downloadRunnables = new CopyOnWriteArrayList<>();
        downloadRunnables.addAll(runnables);
        flushRunnable = new FlushRunnable(this, finishRunnable);

        getCallbackDispatcher().fetchStart(this, getInfo().getTotalOffset() > 0 );
        try {
            startSubTask(runnables);
        } catch (InterruptedException e) {
           //ignore
        }
        if(getInfo().getTotalOffset() == getInfo().getTotalLength()){
            DownloadCache.getInstance().deleteInfo(info.getId());
            setStatus(DownloadCache.COMPLETE);
            getCallbackDispatcher().taskEnd(this, EndCause.COMPLETED,null);
        }else {
            if(causeThrowable != null){
                setStatus(DownloadCache.STOP);
                getCallbackDispatcher().taskEnd(this, EndCause.ERROR,causeThrowable);
            }else {
                setStatus(DownloadCache.STOP);
                getCallbackDispatcher().taskEnd(this, EndCause.CANCELED,null);
            }

        }


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


    public void setIsStoped(boolean isStoped) {
        this.isStoped.set(isStoped);
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
        return isStoped.get();
    }

    public void dispatchCancel() {

    }

    public int getRunnableSize() {
        if (downloadRunnables == null) {
            return 0;
        }
        return downloadRunnables.size();
    }

    public CopyOnWriteArrayList<AbstractDownloadRunnable> getDownloadRunnables() {
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
    public void setDownloadListener( DownloadListener listener) {
        setDownloadListener(true,listener);
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


    public void stop(){
        if(taskStatus.compareAndSet(DownloadCache.RUNNING,DownloadCache.STOP)){
            setIsStoped(true);
        }else {
            throw new IllegalStateException(" task allready stoped");
        }


    }

    public boolean isSuccess() {
        return isSuccess.get();
    }

    public void setThrowable(Exception e) {
        this.causeThrowable = e;
    }
}
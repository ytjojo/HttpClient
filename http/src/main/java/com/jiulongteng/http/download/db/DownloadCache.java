package com.jiulongteng.http.download.db;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.IInspectNetPolicy;
import com.jiulongteng.http.download.Util;
import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.internal.platform.Platform;


public class DownloadCache implements BreakpointStore {

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int COMPLETE = 2;
    public static final int STOP = 3;
    volatile ExecutorService executorService;
    private int maxRunningTaskCount = 5;
    private volatile static DownloadCache sInstance;
    private int progressDispatchInterval = 100;
    private ConcurrentHashMap<String, DownloadTask> allTasks = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<DownloadTask> pendingQueue = new ConcurrentLinkedDeque<>();
    private LinkedBlockingQueue<DownloadTask> runningQueue = new LinkedBlockingQueue<>(maxRunningTaskCount);

    private int syncBufferSize = 65536;

    private boolean isAndroid = Platform.isAndroid();

    private static Context sContext;
    IInspectNetPolicy inspectNetPolicy;
    private BreakpointStore breakpointStore = new BreakpointStore() {
        @Override
        public void saveBlockInfo(List<BlockInfo> blockInfo, BreakpointInfo breakpointInfo) {

        }

        @Override
        public List<BlockInfo> loadBlockInfo(BreakpointInfo info) {
            return null;
        }

        @Override
        public void updateBlockInfo(int blockInfoId, long currentOffset) {

        }

        @Override
        public void deleteInfo(int downloadInfoId) {

        }

        @Override
        public void saveDownloadInfo(BreakpointInfo info) {

        }

        @Override
        public BreakpointInfo loadDownloadInfo(String url) {
            return null;
        }

        @Override
        public void updateDownloadInfo(BreakpointInfo info) {

        }

        @Override
        public List<BreakpointInfo> loadAllDownloadInfo() {
            return null;
        }
    };

    public static void setContext(Context context){
        sContext = context;
        Dao.init(context);
        DownloadCache.getInstance().setBreakpointStore(Dao.getInstance());
    }
    public static Context getContext(){
        return sContext;
    }

    public static DownloadCache getInstance() {
        if (sInstance == null) {
            synchronized (DownloadCache.class) {
                if (sInstance == null) {
                    sInstance = new DownloadCache();
                }
            }
        }
        return sInstance;
    }

    public void enqueueTask(DownloadTask task) {
        getExecutorService().execute(new Runnable() {
            @Override
            public void run() {
                enqueueInternal(task);
            }
        });
    }


    public synchronized void enqueueInternal(DownloadTask task) {
        if (checkSameTask(task)) {
            task.getCallbackDispatcher().taskEnd(task, EndCause.SAME_TASK_BUSY, null);
            return;
        }
        if (!allTasks.containsKey(task.getUrl())) {
            allTasks.put(task.getUrl(), task);
            if (!runningQueue.offer(task)) {
                pendingQueue.offer(task);
                saveNewDownloadInfo(task);
            } else {
                startTaskInternal(task);
            }
        } else {

            ArrayList<DownloadTask> pendingList = new ArrayList<>();
            pendingList.addAll(pendingQueue);
            ArrayList<DownloadTask> runningList = new ArrayList<>();
            runningList.addAll(runningQueue);
            if (pendingList.contains(task) || runningList.contains(task)) {
                return;
            }
            if (task.getTaskStatus() != RUNNING && runningQueue.offer(task)) {
                startTaskInternal(task);
            }

        }
    }

    public synchronized boolean checkSameTask(DownloadTask task) {
        ArrayList<DownloadTask> tasks = new ArrayList<>();
        tasks.addAll(allTasks.values());
        for (DownloadTask other : tasks) {
            if (task == other) {
                continue;
            }
            if (other.equals(task)) {
                return true;
            }
        }
        return false;
    }

    public boolean submitTask(DownloadTask task, boolean enqueueWhenFull) {
        if (allTasks.containsKey(task.getUrl())) {
            if (runningQueue.offer(task)) {
                getExecutorService().execute(new Runnable() {
                    @Override
                    public void run() {
                        startTaskInternal(task);
                    }
                });
                return true;
            } else {
                if (enqueueWhenFull) {
                    pendingQueue.offer(task);
                }
            }
        }
        return false;

    }

    private void saveNewDownloadInfo(DownloadTask task) {
        BreakpointInfo info = new BreakpointInfo(-1, task.getUrl(), null, task.getParentFile(),
                task.getFilename(), task.isFilenameFromResponse());
        task.setInfo(info);
        breakpointStore.saveDownloadInfo(info);
    }

    protected void startTaskInternal(DownloadTask task) {
        onStart(task);
        task.execute();

    }


    private void onStart(DownloadTask task) {
        task.setStatus(RUNNING);
    }

    public void onComplete(DownloadTask task, boolean startNext) {
        task.setStatus(COMPLETE);
        allTasks.remove(task.getUrl());
        runningQueue.remove(task);
        if (startNext) {
            startNext();
        }
    }

    public void onStop(DownloadTask task, boolean startNext) {
        task.setStatus(STOP);
        runningQueue.remove(task);
        pendingQueue.remove(task);
        if (startNext) {
            startNext();
        }
    }

    private void startNext() {
        DownloadTask next = pendingQueue.peek();
        if (next != null) {
            if (runningQueue.offer(next)) {
                pendingQueue.remove(next);
                startTaskInternal(next);
            }

        }
    }

    public void onStopAllTasks() {
        if (runningQueue.size() == 0) {
            return;
        }

        ArrayDeque<DownloadTask> deque = new ArrayDeque<>();
        deque.addAll(runningQueue);
        DownloadTask task = null;
        while ((task = deque.pollLast()) != null) {
            task.stop();
        }

    }

    public void remove(DownloadTask task, boolean deleteFile) {
        if (runningQueue.contains(task)) {
            return;
        }

        if (deleteFile && task.getFile() != null && task.getFile().exists()) {
            task.getFile().delete();
            breakpointStore.deleteInfo(task.getInfo().getId());
        }
        pendingQueue.remove(task);
        allTasks.remove(task.getUrl());
    }

    public void remove(String url, boolean deleteFile) {
        DownloadTask task = allTasks.remove(url);
        if (task == null) {
            return;
        }
        remove(task, deleteFile);
    }

    public void removeAll(boolean deleteFile) {

    }

    public void setMaxRunningTaskCount(int maxRunningTaskCount) {
        if (!runningQueue.isEmpty()) {
            return;
        }
        this.maxRunningTaskCount = maxRunningTaskCount;
        this.runningQueue = new LinkedBlockingQueue<>(maxRunningTaskCount);
    }

    @Override
    public void saveBlockInfo(List<BlockInfo> blockInfo, BreakpointInfo breakpointInfo) {
        breakpointStore.saveBlockInfo(blockInfo, breakpointInfo);
    }

    @Override
    public List<BlockInfo> loadBlockInfo(BreakpointInfo info) {
        return breakpointStore.loadBlockInfo(info);
    }

    @Override
    public void updateBlockInfo(int blockInfoId, long currentOffset) {
        breakpointStore.updateBlockInfo(blockInfoId, currentOffset);
    }

    @Override
    public void deleteInfo(int downloadInfoId) {
        breakpointStore.deleteInfo(downloadInfoId);
    }

    @Override
    public void saveDownloadInfo(BreakpointInfo info) {
        breakpointStore.saveDownloadInfo(info);
    }

    @Override
    public BreakpointInfo loadDownloadInfo(String url) {
        return breakpointStore.loadDownloadInfo(url);
    }

    @Override
    public void updateDownloadInfo(BreakpointInfo info) {
        breakpointStore.updateDownloadInfo(info);
    }

    @Override
    public List<BreakpointInfo> loadAllDownloadInfo() {
        return breakpointStore.loadAllDownloadInfo();
    }

    public void setBreakpointStore(BreakpointStore breakpointStore) {
        this.breakpointStore = breakpointStore;
    }

    public synchronized boolean isFileConflictAfterRun(@NonNull DownloadTask task) {
        final File file = task.getFile();
        if (file == null) return false;

        ArrayList<DownloadTask> tasks = new ArrayList<>();
        tasks.addAll(allTasks.values());

        // Other one is running, cancel the current task.
        for (DownloadTask downloadTask : tasks) {
            if (downloadTask == task) {
                continue;
            }

            final File otherFile = downloadTask.getFile();
            if (otherFile != null && file.equals(otherFile)) {
                return true;
            }
        }
        return false;

    }

    public void setProgressDispatchInterval(int progressDispatchInterval) {
        this.progressDispatchInterval = progressDispatchInterval;
    }

    public int getProgressDispatchInterval() {
        return progressDispatchInterval;
    }


    synchronized ExecutorService getExecutorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                    Util.threadFactory("Download manager", false));
        }

        return executorService;
    }

    public void setSyncBufferSize(int syncBufferSize) {
        this.syncBufferSize = syncBufferSize;
    }

    public int getSyncBufferSize() {
        return syncBufferSize;
    }

    public boolean isAndroid(){
        return isAndroid;
    }

    public IInspectNetPolicy getInspectNetPolicy() {
        return inspectNetPolicy;
    }

    public void setInspectNetPolicy(IInspectNetPolicy inspectNetPolicy) {
        this.inspectNetPolicy = inspectNetPolicy;
    }

    public boolean isNetPolicyValid(DownloadTask task){
        if(inspectNetPolicy != null){
            return inspectNetPolicy.isNetPolicyValid(task);
        }
        return true;
    }
}

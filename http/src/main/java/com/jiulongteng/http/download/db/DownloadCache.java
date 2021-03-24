package com.jiulongteng.http.download.db;

import androidx.annotation.NonNull;

import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.entry.BlockInfo;
import com.jiulongteng.http.download.entry.BreakpointInfo;

import java.io.File;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.LinkedBlockingQueue;

public class DownloadCache implements BreakpointStore {

    public static final int PENDING = 0;
    public static final int RUNNING = 1;
    public static final int COMPLETE = 2;
    public static final int STOP = 3;
    private int maxRunningTaskCount = 5;
    private volatile static DownloadCache sInstance;
    private ConcurrentHashMap<String, DownloadTask> allTasks = new ConcurrentHashMap<>();
    private ConcurrentLinkedDeque<DownloadTask> pendingQueue = new ConcurrentLinkedDeque<>();
    private LinkedBlockingQueue<DownloadTask> runningQueue = new LinkedBlockingQueue<>(maxRunningTaskCount);
    private BreakpointStore breakpointStore = new BreakpointStore() {
        @Override
        public void saveBlockInfo(List<BlockInfo> blockInfo, BreakpointInfo breakpointInfo) {

        }

        @Override
        public List<BlockInfo> getBlockInfo(BreakpointInfo info) {
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
        public BreakpointInfo getDownloadInfo(String url) {
            return null;
        }

        @Override
        public void updateDownloadInfo(BreakpointInfo info) {

        }
    };

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


    public synchronized void add(DownloadTask task) {
        if (!allTasks.containsKey(task.getUrl())) {
            allTasks.put(task.getUrl(), task);
            if (!runningQueue.offer(task)) {
                pendingQueue.offer(task);
            } else {
                submitTask(task);
            }
        }else {
            task.getCallbackDispatcher().taskEnd(task, EndCause.SAME_TASK_BUSY,null);
        }
    }

    private void submitTask(DownloadTask task) {
        onStart(task);
        task.execute();

    }


    public void onStart(DownloadTask task) {
        task.setStatus(RUNNING);
    }

    public void onComplete(DownloadTask task, boolean startNext) {
        task.setStatus(COMPLETE);
        allTasks.remove(task.getUrl());
        runningQueue.remove(task);
        if (startNext && runningQueue.remove(task)) {
            startNext();
        }
    }

    public void onStop(DownloadTask task, boolean startNext) {
        task.setStatus(STOP);
        allTasks.remove(task.getUrl());
        runningQueue.remove(task);
        if (startNext && runningQueue.remove(task)) {
            startNext();
        }
    }

    public void startNext() {
        DownloadTask next = pendingQueue.peek();
        if (next != null) {
            if (runningQueue.offer(next)) {
                pendingQueue.remove(next);
                submitTask(next);
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
            task.setStatus(STOP);
            pendingQueue.offerFirst(task);
        }

    }

    public void remove(DownloadTask task, boolean deleteFile) {
        if (runningQueue.contains(task)) {
            return;
        }
        breakpointStore.deleteInfo(task.getInfo().getId());
        if (deleteFile && task.getFile() != null && task.getFile().exists()) {
            task.getFile().delete();
        }
        pendingQueue.remove(task);
        allTasks.remove(task.getUrl());
        breakpointStore.deleteInfo(task.getInfo().getId());
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
        this.maxRunningTaskCount = maxRunningTaskCount;
    }

    @Override
    public void saveBlockInfo(List<BlockInfo> blockInfo, BreakpointInfo breakpointInfo) {
        breakpointStore.saveBlockInfo(blockInfo, breakpointInfo);
    }

    @Override
    public List<BlockInfo> getBlockInfo(BreakpointInfo info) {
        return breakpointStore.getBlockInfo(info);
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
    public BreakpointInfo getDownloadInfo(String url) {
        return breakpointStore.getDownloadInfo(url);
    }

    @Override
    public void updateDownloadInfo(BreakpointInfo info) {
        breakpointStore.updateDownloadInfo(info);
    }

    public void setBreakpointStore(BreakpointStore breakpointStore) {
        this.breakpointStore = breakpointStore;
    }

    public synchronized boolean isFileConflictAfterRun(@NonNull DownloadTask task) {
        final File file = task.getFile();
        if (file == null) return false;

        // Other one is running, cancel the current task.
        for (DownloadTask downloadTask : allTasks.values()) {
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
}

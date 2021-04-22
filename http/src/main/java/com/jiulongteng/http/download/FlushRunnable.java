package com.jiulongteng.http.download;

import com.jiulongteng.http.download.db.DownloadCache;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class FlushRunnable implements Runnable {
    private static final String TAG = "FlushRunnable";

    private static final ExecutorService FILE_IO_EXECUTOR = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            Util.threadFactory("Download file io", false));

    private DownloadTask task;
    ConcurrentHashMap<Integer, AbstractDownloadRunnable> downloadRunnableMap = new ConcurrentHashMap<>();
    AtomicReference<Thread> parkThreadRef = new AtomicReference(null);

    private volatile Future syncFuture;

    AtomicLong allNoSyncLength = new AtomicLong(0);

    HashSet<Integer> finishedIndex = new HashSet<>();
    Runnable finishRunnable;
    long memoryBufferSize = 8096;
    public FlushRunnable(DownloadTask task, Runnable finishRunnable) {
        this.task = task;
        this.finishRunnable = finishRunnable;
    }

    @Override
    public void run() {
        parkThreadRef.set(Thread.currentThread());
        flushAll();
        while (true) {
            parkThread(300);
            flushAll();
            if (task.isStoped()) {
                Util.i(TAG,  downloadRunnableMap.size() + " all size " + task.getRunnableSize()+ "   loop " + finishedIndex.size());
            }
            if (task.getRunnableSize() == finishedIndex.size()) {
                if (finishRunnable != null) {
                    FILE_IO_EXECUTOR.submit(finishRunnable);
                }
                Util.i(TAG, " finishSize " + finishedIndex.size());
                break;
            }
        }
    }

    private void wakeup() {
        if (syncFuture == null) {
            synchronized (this) {
                if (syncFuture == null) {
                    syncFuture = executeSyncRunnableAsync();
                }
            }
        }
        Thread thread = parkThreadRef.get();
        if (thread != null) {
            unparkThread(thread);

        }

    }

    private Future executeSyncRunnableAsync() {
        return FILE_IO_EXECUTOR.submit(this);
    }

    private void flushAll() {
        HashMap<Integer,AbstractDownloadRunnable> errorMap =new HashMap<>();
        boolean needSyncBuffer = allNoSyncLength.get() >= DownloadCache.getInstance().getSyncBufferSize();
        boolean synced = false;
        for (int i = 0; i < task.getRunnableSize(); i++) {
            AbstractDownloadRunnable downloadRunnable = downloadRunnableMap.remove(i);
            if (downloadRunnable != null) {
                boolean isReadByteFinished = downloadRunnable.getIsReadByteFinished();
                try {
                    if(isReadByteFinished || needSyncBuffer){
                        synced = true;
                        downloadRunnable.flush();
                    }
                } catch (IOException e) {
                    Util.e(TAG, "buffered "+downloadRunnable.getBufferedLength() +" AbstractDownloadRunnable flush error index = " + downloadRunnable.getIndex(), e);
                    e.printStackTrace();
                    errorMap.put(i,downloadRunnable);
                }finally {
                    if(isReadByteFinished){
                        finishedIndex.add(i);
                        downloadRunnable.unPark();
                        Util.i(TAG, "  finishSize = " + finishedIndex.size() + "  index = " + downloadRunnable.getIndex());
                    }
                }
            }
        }
        if(synced){
            task.getCallbackDispatcher().fetchProgress(task);
        }
//        if(!errorMap.isEmpty()){
//            downloadRunnableMap.putAll(errorMap);
//        }else {
//        }



    }

    public void flush(AbstractDownloadRunnable downloadRunnable,long byteRead) {
        downloadRunnableMap.put(downloadRunnable.getIndex(), downloadRunnable);
        allNoSyncLength.addAndGet(byteRead);
        wakeup();
    }

    public void done(AbstractDownloadRunnable downloadRunnable) {
        downloadRunnableMap.put(downloadRunnable.getIndex(),downloadRunnable);
        wakeup();
    }

    // convenient for test
    void parkThread(long milliseconds) {
        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(milliseconds));
    }

    void parkThread() {
        LockSupport.park();
    }

    // convenient for test
    void unparkThread(Thread thread) {
        LockSupport.unpark(thread);
    }


}

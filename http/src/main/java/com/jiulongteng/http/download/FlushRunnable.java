package com.jiulongteng.http.download;

import com.jiulongteng.http.download.db.DownloadCache;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class FlushRunnable implements Runnable {
    private static final String TAG = "FlushRunnable";

    private static final ExecutorService FILE_IO_EXECUTOR = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE,
            60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
            Util.threadFactory("OkDownload file io", false));

    private DownloadTask task;
    ConcurrentHashMap<Integer,AbstractDownloadRunnable> downloadRunnableMap = new ConcurrentHashMap<>();
    LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();
    int finishSize=0;
    AtomicReference<Thread> parkThreadRef =new AtomicReference(null);

    private volatile Future syncFuture;

    private final ReentrantLock takeLock = new ReentrantLock();

    private final Condition notEmpty = takeLock.newCondition();

    Runnable finishRunnable;
    public FlushRunnable(DownloadTask task,Runnable finishRunnable) {
        this.task = task;
        this.finishRunnable = finishRunnable;
    }

    @Override
    public void run() {
        flushAll();
        parkThreadRef.set(Thread.currentThread());
        while (true){
            parkThread();
            flushAll();
            if(task.isStoped()){
                Util.i(TAG,"   loop " + finishSize);
            }
            if(task.getRunnableSize() == finishSize){
                if(finishRunnable!= null){
                    FILE_IO_EXECUTOR.submit(finishRunnable);
                }
                Util.i(TAG, " finishSize ");
                break;
            }
        }
    }

    private void wakeup(){
        if(syncFuture == null){
            synchronized (this){
                if(syncFuture == null){
                    syncFuture = executeSyncRunnableAsync();
                }
            }
        }
        Thread thread = parkThreadRef.get();
        if(thread != null){
            unparkThread(thread);

        }

    }

    private Future executeSyncRunnableAsync(){
        return FILE_IO_EXECUTOR.submit(this);
    }

    private void flushAll(){

        if(task.isStoped()){
            Runnable runnable;
            while (true){
                try {
                    runnable = blockingQueue.take();
                    runnable.run();
                    Util.i(TAG," flushAll "+finishSize + "  ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(task.getRunnableSize() == finishSize){
                    break;
                }
            }
        }else {
            for (int i = 0; i < task.getRunnableSize(); i++) {
                AbstractDownloadRunnable downloadRunnable = downloadRunnableMap.remove(i);
                if(downloadRunnable != null){
                    try {
                        downloadRunnable.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }
        }



    }
    public void flush(AbstractDownloadRunnable downloadRunnable){
        downloadRunnableMap.put(downloadRunnable.getIndex(),downloadRunnable);
        wakeup();
    }

    public void done(AbstractDownloadRunnable downloadRunnable){
        blockingQueue.offer(new Runnable() {
            @Override
            public void run() {
                try{
                    downloadRunnable.flush();

                }catch (IOException e){

                }finally {
                    finishSize++;
                    downloadRunnable.unPark();
                    Util.i(TAG,"  "+finishSize + "  done " + downloadRunnable.getIndex());
                }

            }
        });
        if(task.isStoped()){
            Util.i(TAG,"wake up " + downloadRunnable.getIndex());
        }
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

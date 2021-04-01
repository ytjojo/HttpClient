package com.jiulongteng.http.download;

import android.nfc.Tag;

import com.jiulongteng.http.download.entry.BlockInfo;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

public abstract class AbstractDownloadRunnable implements Runnable {
    protected DownloadTask task;
    protected BlockInfo blockInfo;

    private Thread currentThread;
    private int index;
    private long contentLength;

    private long bufferMax; //缓冲区允许放置的字节级数据量
    private AtomicLong bufferedLength;    //缓冲区中未刷入内存的大小即缓冲区写入模式下的起始位置


    long readLength;
    private AtomicBoolean  isReadByteFinished = new AtomicBoolean(false);;

    public AbstractDownloadRunnable(DownloadTask task, BlockInfo blockInfo, int index) {
        this.blockInfo = blockInfo;
        this.task = task;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public abstract void flush() throws IOException;



    public DownloadTask getTask() {
        return task;
    }


    public BlockInfo getBlockInfo() {
        return blockInfo;
    }
    public void unPark(){
        if(currentThread != null){
            LockSupport.unpark(currentThread);
        }
    }
    public void parkThread(){
        this.currentThread = Thread.currentThread();
        LockSupport.park();
    }
    public void setCurrentThread(){
        this.currentThread = Thread.currentThread();
        currentThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
                Util.e("UncaughtExceptionHandler","error" + index , e);
            }
        });
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public void setIsReadByteFinished(boolean isReadByteFinished) {
        this.isReadByteFinished.set(isReadByteFinished);
    }

    public boolean getIsReadByteFinished() {
        return isReadByteFinished.get();
    }
    public void interrupt(){
        if(currentThread != null){
            currentThread.interrupt();
        }

    }

    public void setBufferMax(long bufferMax) {
        this.bufferMax = bufferMax;
    }

    public long getBufferMax() {
        return bufferMax;
    }

    public void setBufferedLength(long bufferedLength) {
        if(this.bufferedLength == null){
            this.bufferedLength = new AtomicLong(bufferedLength);
        }else {
            this.bufferedLength.set(bufferedLength);
        }
    }
    public long addAndGetBufferedLength(long delta){
        return bufferedLength.addAndGet(delta);
    }


    public long getBufferedLength() {
        if(bufferedLength == null){
            return 0;
        }
        return bufferedLength.get();
    }

    public int getBufferSize(){
        return 8096;
    }
}

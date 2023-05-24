package com.jiulongteng.http.download;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.rx.SimpleObserver;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DownloadTaskTest extends TestCase {
    long start;
    long stop;

    @Test
    public void testExecute() throws InterruptedException {
        Request request = new Request.Builder().url("http://update.myweimai.com/wemay.apk")
                .build();
        Util.setLogger(new Util.Logger() {
            @Override
            public void e(String tag, String msg, Throwable e) {
                System.out.println(tag + msg + e.getMessage());
            }

            @Override
            public void w(String tag, String msg) {
                System.out.println(tag + msg );
            }

            @Override
            public void d(String tag, String msg) {
                System.out.println(tag + msg );
            }

            @Override
            public void i(String tag, String msg) {
                System.out.println(tag + msg );
            }
        });
        DownloadTask task = new DownloadTask(new File("/Users/jiulongteng/Downloads"),new OkHttpClient(),request,5);
        task.setFinishRunnable(new Runnable() {
            @Override
            public void run() {

            }
        });
        task.setDownloadListener( new DownloadListener() {
            @Override
            public void taskStart(@NonNull DownloadTask task) {
                System.out.println("taskStart");
                start = System.currentTimeMillis();
            }

            @Override
            public void connectTrialStart(@NonNull DownloadTask task) {
                System.out.println("connectTrialStart");
            }

            @Override
            public void fetchStart(@NonNull DownloadTask task, boolean isFromBeginning) {
                System.out.println("fetchStart");


            }

            @Override
            public void fetchProgress(@NonNull DownloadTask task, int currentProgress, long currentSize, long contentLength,long speed) {
                System.out.println("fetchProgress" + currentProgress + " currentSize "+ currentSize + " contentLength " + contentLength + " speed " +speed);
//                if(currentProgress > 10){
//                    task.stop();
//                    stop = System.currentTimeMillis();
//                    System.out.println("----------after stop");
//                }
            }

            @Override
            public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Throwable realCause) {
                if(realCause != null){
                    realCause.printStackTrace();
                }
                System.out.println("taskEnd  stop =" + (System.currentTimeMillis()   - stop)  + "  total = " +  + (System.currentTimeMillis() - start));
            }
        });
        task.setSpeedListener(new SpeedListener() {
            @Override
            public void onProgress(DownloadTask task, SpeedCalculator speedCalculator) {
                System.out.println(speedCalculator.getInstantBytesPerSecondAndFlush() +  " speed " + speedCalculator.getBytesPerSecondFromBegin());
            }

        });

        DownloadCache.getInstance().enqueueInternal(task);
    }


}
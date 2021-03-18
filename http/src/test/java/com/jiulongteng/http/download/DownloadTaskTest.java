package com.jiulongteng.http.download;

import junit.framework.TestCase;

import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class DownloadTaskTest extends TestCase {

    @Test
    public void testExecute() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Request request = new Request.Builder().url("http://update.myweimai.com/wemay.apk")
                .build();
        DownloadTask task = new DownloadTask(new File("/Users/jiulongteng/Downloads"),new OkHttpClient(),request,3);
        task.setFinishRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        });
        task.execute();
        countDownLatch.await();
    }
}
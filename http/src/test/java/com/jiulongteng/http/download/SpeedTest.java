package com.jiulongteng.http.download;

import com.jiulongteng.http.rx.SimpleObserver;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.functions.Consumer;

public class SpeedTest {

    long last ;
    long start;
    @Test
    public void speedTest() throws InterruptedException {
        SpeedCalculator speedCalculator = new SpeedCalculator();
        CountDownLatch countDownLatch = new CountDownLatch(1);




        countDownLatch.await();
    }

}

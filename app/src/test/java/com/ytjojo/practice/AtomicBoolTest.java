package com.ytjojo.practice;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2016/11/9 0009.
 */
public class AtomicBoolTest {
    @Test
    public void test() throws InterruptedException {
        CountDownLatch mCountDownLatch = new CountDownLatch(1);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Assert.assertEquals(true,atomicBoolean.compareAndSet(false,true));
                mCountDownLatch.countDown();
            }
        }.start();
        mCountDownLatch.await();
        Assert.assertEquals(true,atomicBoolean.compareAndSet(true,false));
        Assert.assertEquals(false,atomicBoolean.get());


    }
}

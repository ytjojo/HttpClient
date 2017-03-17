package com.ytjojo.practice;

import android.support.v4.util.Pair;

import com.ytjojo.rx.RollingReplaySubject;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.PublishSubject;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Administrator on 2016/11/7 0007.
 */
public class RollingReplaySubjectTest {

    private Observable<Object> observable;
    @Before
    public void setUp(){
        observable = PublishSubject.create();
    }
    @Test
    public void testReplay() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        RollingReplaySubject replaySubject = new RollingReplaySubject();
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
         }.start();
        replaySubject.subscribeTail(new Subscriber() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {
                System.out.println("-1aaa  =" +o);

            }
        });
        replaySubject.onNext(1);
        replaySubject.onNext(2);
        replaySubject.subscribeTypeTail(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String o) {
                System.out.println("String ========" +o);
            }
        },String.class);
        replaySubject.onNext(3);
        replaySubject.clear();
        replaySubject.onNext(4);


//        replaySubject.subscribePair(new Subscriber<String>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//            }
//
//            @Override
//            public void onNext(String o) {
//                System.out.println("pair" +o);
//            }
//        },String.class,"key");
        replaySubject.onNext(5);
        replaySubject.clear();
        replaySubject.onNext(6);
        replaySubject.onNext("杨腾蛟");
        replaySubject.onNext(new Pair<String,String>("key","张三"));
        replaySubject.onNext(new Pair<String,String>("key","李四"));
        replaySubject.onNext("zhangsan");
        countDownLatch.await();
        assertEquals(4, 2 + 2);

    }
}

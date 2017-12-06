package com.ytjojo.practice;

import com.ytjojo.http.exception.AuthException;
import com.ytjojo.rx.RxBus;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/8/7 0007.
 */

public class Rxtest {


    @Test
    public void errortest() {
        getObservable().subscribeOn(Schedulers.io())
//                .retryWhen(attempts -> {
//            return attempts.zipWith(Observable.range(1, 3), (n, i) -> i).flatMap(i -> {
//                System.out.println("delay retry by " + i + " second(s)");
//                return Observable.timer(i, TimeUnit.SECONDS);
//            });
//        })
                .retryWhen(getRetryFunc1())
//                .forEach(new Action1<Integer>() {
//                    @Override
//                    public void call(Integer integer) {
//
//                    }
//                })
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        System.out.println( "onCompleted--" + Thread.currentThread().getName());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        System.out.println("integer" + integer);
                    }
                });
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Observable<Integer> getObservable() {
        return Observable.unsafeCreate(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
//                subscriber.onNext(1);
//                subscriber.onNext(2);
//                subscriber.onNext(3);
                System.out.println("call");
                System.out.println( "currentThread--" + Thread.currentThread().getName());
//                subscriber.onError(new NullPointerException());
                subscriber.onError(new IllegalArgumentException());
                subscriber.onCompleted();

            }
        });
    }

    public static Func1<Observable<? extends Throwable>, Observable<?>> getRetryFunc1() {
        return new Func1<Observable<? extends Throwable>, Observable<?>>() {
            private int retryDelaySecond = 5;
            private int retryCount = 0;
            private int maxRetryCount = 3;

            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        return checkApiError(throwable);
                    }
                });
            }

            private Observable<?> checkApiError(Throwable throwable) {
                retryCount++;
                System.out.println(retryCount + "retryCount--" + Thread.currentThread().getName());

                if (throwable instanceof IllegalArgumentException) {
                    return retry(true);
                } else if (throwable instanceof NullPointerException) {
                    return retry(false);
                }
                return Observable.error(throwable);
            }

            private Observable<?> retry(boolean throwError) {

                if (retryCount <= maxRetryCount) {
                    return Observable.timer(retryDelaySecond,
                            TimeUnit.SECONDS).observeOn(Schedulers.newThread());
                } else {
                    if(throwError){
                        return Observable.error(new AuthException(-100, "token超时"));
                    }else{
                        return Observable.error(new AuthException(-100, "token超时"));
                    }
//                    return Observable.error(new AuthException(-100, "token超时"));
                }
            }
        };
    }

    @Test
    public void Rxbustext(){
        Subscription s1 = RxBus.getDefault().registerObservable(Integer.class).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer+" s 1");
            }
        });
        Subscription s2 =RxBus.getDefault().registerObservable(Integer.class).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer+" s 2");
            }
        });
       Subscription s3 = RxBus.getDefault().registerObservable(Integer.class).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer+" s 3");
            }
        });
        RxBus.getDefault().registerObservable(Integer.class).subscribe( new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer+" s 4");
            }
        });

        RxBus.getDefault().registerObservable(Integer.class).subscribe( new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                System.out.println(integer+" s 5");
            }
        });

        RxBus.getDefault().post(100);
    }
}

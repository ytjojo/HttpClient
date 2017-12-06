package com.ytjojo.http;

import com.ytjojo.http.exception.AuthException;
import com.ytjojo.http.exception.TokenInvalidException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import retrofit2.ProxyHandler;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public abstract class TokenRefreshHandler {


    private volatile boolean isTokenAreadyUpdate;


    Throwable mAuthThrwable;

    public <T> Observable<T> getObservable(Callable<T> callable){
       Observable<T> observable = Observable.unsafeCreate(new Observable.OnSubscribe<T>() {
            @Override public void call(Subscriber<? super T> subscriber) {
                try {
                    T value =  callable.call();
                    subscriber.onNext(value);

                } catch (Exception e) {
                    subscriber.onError(e);
                    e.printStackTrace();
                }
            }
        });
        return observable.retryWhen(getRetryFunc1());
    }

    private Func1<Observable<? extends Throwable>, Observable<?>> getRetryFunc1(){
        return new Func1<Observable<? extends Throwable>, Observable<?>>() {
            private int retryDelaySecond =5;
            private int retryCount =0;
            private int maxRetryCount =3;
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
                System.out.println(retryCount+"retryCount--"+Thread.currentThread().getName());
                if (throwable instanceof TokenInvalidException) {
                    return checkTokenUpdating();
                } else {
                    return Observable.error(throwable);
                }

            }

            private Observable<?> checkTokenUpdating() {

                if(!isTokenAreadyUpdate){
                    synchronized (ProxyHandler.class){
                        if(!isTokenAreadyUpdate){
                            mAuthThrwable = null;
                            getTokenObsevable().subscribe(new Action1<String>() {
                                @Override
                                public void call(String s) {

                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    mAuthThrwable = throwable;
                                    if(!(mAuthThrwable instanceof AuthException))
                                        mAuthThrwable = new AuthException("重新请求token失败",throwable);
                                }
                            });
                            isTokenAreadyUpdate = true;
                            if (mAuthThrwable != null) {
                                return  Observable.error(mAuthThrwable);
                            } else {
                                if(retryCount >maxRetryCount){
                                    return  Observable.error(new AuthException(-100,"token超时"));
                                }
                                return Observable.just(true);
                            }
                        }else {
                            System.out.println(retryCount+"retryCount"+Thread.currentThread().getName());
                            if(retryCount<maxRetryCount){
                                return Observable.timer(retryCount * retryDelaySecond,
                                        TimeUnit.SECONDS).observeOn(Schedulers.io());
                            }else{
                                return Observable.error(new AuthException(-100,"token超时"));
                            }

                        }
                    }
                }

                if(retryCount<maxRetryCount){
                        //return  Observable.timer(retryCount * retryDelaySecond,
                        //        TimeUnit.SECONDS);
                    return Observable.just(true);
                }else{
                    return  Observable.error(new AuthException(-100,"token超时"));
                }
            }
        };
    }


    public abstract Observable<String> getTokenObsevable();
}
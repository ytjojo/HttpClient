package com.ytjojo.repertory;

import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.exception.AuthException;
import com.ytjojo.http.exception.TokenInvalidException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ProxyHandler implements InvocationHandler {


    private final long MIN_UPDATEDELAY = 30000 ;
    private boolean isTokenAreadyUpdate;

    public ProxyHandler(Object object){
        this.mObject = object;
    }

    private Object mObject;

    public void setObject(Object obj) {
        this.mObject = obj;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        return null;
    }


    Throwable mAuthThrwable;

    public <T> Observable<T> getObservable(T value){
        return Observable.just(true)
            .flatMap(new Func1<Object, Observable<T>>() {
                @Override
                public Observable<T> call(Object o) {
                    return Observable.just(value);
                }
            }).retryWhen(getRetryFunc1(), Schedulers.trampoline());
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
                                    RetrofitClient.TOKEN = s;

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
                                return Observable.just(true);
                            }
                        }else {
                            if(retryCount++<maxRetryCount){
                                return Observable.timer(retryCount * retryDelaySecond,
                                        TimeUnit.SECONDS);
                            }else{
                                return Observable.error(new AuthException(-100,"token超时"));
                            }

                        }
                    }
                }
                if(retryCount++<maxRetryCount){
//                        return  Observable.timer(retryCount * retryDelaySecond,
//                                TimeUnit.SECONDS);
                    return Observable.just(true);
                }else{
                    return  Observable.error(new AuthException(-100,"token超时"));
                }
            }
        };
    }


    public Observable<String> getTokenObsevable() {
        return null;
    }
}
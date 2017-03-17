package com.ytjojo.repertory;

import android.os.SystemClock;

import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.exception.APIException;
import com.ytjojo.http.exception.AuthException;
import com.ytjojo.http.exception.TokenInvalidException;
import com.ytjojo.utils.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import retrofit2.http.Header;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ProxyHandler implements InvocationHandler {


    private final long MIN_UPDATEDELAY = 30000 ;
    ApiServiceProxy.TokenObservable mTokenObservable;
    public ProxyHandler(ApiServiceProxy.TokenObservable observable, Object object){
        this.mTokenObservable = observable;
        this.mObject = object;
    }
    private Object[] replceToken(Method method, final Object[] args, String newToken) {
        Annotation[][] annotationsArray = method.getParameterAnnotations();
        Annotation[] annotations = null;
        Annotation annotation = null;
        if (annotationsArray != null && annotationsArray.length > 0) {
            for (int i = 0; i < annotationsArray.length; i++) {
                annotations = annotationsArray[i];
                for (int j = 0; j < annotations.length; j++) {
                    annotation = annotations[j];
                    if (annotation instanceof Query) {
                        if (ApiServiceProxy.ACCESS_TOKEN_KEY.equals(((Query) annotation).value())) {
                            args[i] = newToken;
                            break;
                        }
                    }else if(annotation instanceof Header){
                        Header header = (Header) annotation;
                        if(RetrofitClient.TOKEN_HEADER_KEY.equals(header.value())){
                            args[i] = newToken;
                            break;
                        }
                    }
                }
            }
        }
        return  args;
    }

    private Object mObject;

    public void setObject(Object obj) {
        this.mObject = obj;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        Object result = null;
        result = Observable.just(null)
                .flatMap(new Func1<Object, Observable<?>>() {
                    @Override
                    public Observable<?> call(Object o) {
                        try {
                            if(ApiServiceProxy.isTokenAreadyUpdate && !TextUtils.isEmpty(RetrofitClient.TOKEN)){
                                replceToken(method,args,RetrofitClient.TOKEN);
                            }
                            return (Observable<?>) method.invoke(mObject, args);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return Observable.error(new APIException(-100, "method call error"));
                    }
                }).retryWhen(getRetryFunc1(), Schedulers.trampoline());
        return result;
    }


    Throwable mAuthThrwable;

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
                if(ApiServiceProxy.mLastTokenUpdateTime == 0||((SystemClock.elapsedRealtime() - ApiServiceProxy.mLastTokenUpdateTime)>MIN_UPDATEDELAY)){
                    ApiServiceProxy.isTokenAreadyUpdate =false;
                }
                if(!ApiServiceProxy.isTokenAreadyUpdate){
                    synchronized (ProxyHandler.class){
                        if(!ApiServiceProxy.isTokenAreadyUpdate){
                            mAuthThrwable = null;
                            getTokenObsevable().subscribe(new Action1<String>() {
                                @Override
                                public void call(String s) {
                                    RetrofitClient.TOKEN = s;
                                    ApiServiceProxy.mLastTokenUpdateTime = SystemClock.elapsedRealtime();

                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    mAuthThrwable = throwable;
                                    if(!(mAuthThrwable instanceof AuthException))
                                        mAuthThrwable = new AuthException("重新请求token失败",throwable);
                                }
                            });
                            ApiServiceProxy.isTokenAreadyUpdate = true;
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
        return mTokenObservable.getTokenImmediately();
    }
}
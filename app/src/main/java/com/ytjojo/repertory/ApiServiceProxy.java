package com.ytjojo.repertory;

import java.lang.reflect.Proxy;

import retrofit2.Retrofit;
import rx.Observable;

public class ApiServiceProxy {

    public static volatile boolean isTokenAreadyUpdate = false;
    public static final String ACCESS_TOKEN_KEY = "token";
    public static volatile long mLastTokenUpdateTime;
    public static TokenObservable mTokenObservable;
    public static void setTokenObservable(TokenObservable observable){
        mTokenObservable = observable;
    }
    public static  <T>  T getProxy(Retrofit retrofit,Class<T> tClass) {
        T t = retrofit.create(tClass);
        ProxyHandler handler = new ProxyHandler(mTokenObservable,t);
        return (T) Proxy.newProxyInstance(tClass.getClassLoader(), new Class<?>[] { tClass }, handler);
    }
    public interface TokenObservable{
        Observable<String> getTokenImmediately();
    }
}
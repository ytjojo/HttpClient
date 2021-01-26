package com.jiulongteng.http.request;

import androidx.lifecycle.LifecycleOwner;

import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.AbstractClient;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Predicate;
import okhttp3.OkHttpClient;

public class HttpRequest<T> implements IRequest<T> {

    AbstractClient httpClient;
    private boolean isSync;
    HashMap<String,String> params;
    HashMap<String,String> headers;
    HashMap<String,Object> postParams;
    Object postBody;

    OkHttpClient okHttpClient;

    long observeTimeout = -1;
    TimeUnit observeTimeUnit = TimeUnit.MILLISECONDS;


    @Override
    public <T> IRequest<T> asType(TypeToken<T> typeToken) {
        return (IRequest<T> )this;
    }

    @Override
    public IRequest<T> setSync() {
        isSync = true;
        return this;
    }

    @Override
    public IRequest<T> add(String key, String value) {
        if(params == null){
            params = new HashMap<>();
        }
        params.put(key,value);
        return null;
    }

    @Override
    public IRequest<T> add(String key, Object value) {
        if(postParams == null){
            postParams = new HashMap<>();
        }
        postParams.put(key,value);
        return null;
    }

    @Override
    public IRequest<T> add(Object body) {
        this.postBody = body;
        return null;
    }

    @Override
    public IRequest<T> addHeader(String key, String value) {
        if(headers == null){
            headers = new HashMap<>();
        }
        headers.put(key,value);
        return null;
    }

    @Override
    public IRequest<T> https(SSLSocketFactory socketFactory, X509TrustManager trustManager) {

        okHttpClient = okHttpClient.newBuilder().sslSocketFactory(socketFactory,trustManager).build();
        return this;
    }

    @Override
    public IRequest<T> hostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClient = okHttpClient.newBuilder().hostnameVerifier(hostnameVerifier).build();
        return this;
    }

    @Override
    public IRequest<T> timeout(long connectTimeout, long readTimeout, long writeTimeout, TimeUnit timeUnit) {
        okHttpClient = okHttpClient.newBuilder().connectTimeout(connectTimeout,timeUnit)
                .readTimeout(readTimeout,timeUnit)
                .writeTimeout(writeTimeout,timeUnit)
                .build();
        return this;
    }

    @Override
    public IRequest<T> observeTimeout(long observeTimeout, TimeUnit timeUnit) {
        this.observeTimeout = observeTimeout;
        this. observeTimeUnit = timeUnit;
        return this;
    }

    @Override
    public IRequest<T> observeTimeout(long observeTimeout) {
        this.observeTimeout = observeTimeout;
        return this;
    }

    @Override
    public IRequest<T> retry(int retryCount, long interval, Predicate<Throwable> predicate) {
        return null;
    }

    @Override
    public IRequest<T> retry(int retryCount, long interval) {
        return null;
    }

    @Override
    public IRequest<T> observeOnMain() {
        return null;
    }

    @Override
    public IRequest<T> observeOnMain(LifecycleOwner owner) {
        return null;
    }

    @Override
    public Observable<T> toObservable() {
        return null;
    }
}

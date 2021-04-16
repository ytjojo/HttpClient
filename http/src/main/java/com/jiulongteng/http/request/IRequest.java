package com.jiulongteng.http.request;

import androidx.lifecycle.LifecycleOwner;

import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.AbstractClient;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.progress.ProgressListener;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Predicate;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

public interface IRequest<T>{

    IRequest<T> from(AbstractClient client, HttpRequest.HttpMethod method);
    <U> IRequest<U> asType(TypeToken<U> typeToken);
    <U> IRequest<U> asType(Type type);
    HttpRequest<IResult<T>> asResult();
    IRequest<T> setSync();
    IRequest<T> relativeUrl(String relativeUrl);

    IRequest<T> formatUrlParams(Object... params);

    IRequest<T> add(String key,Object value);
    IRequest<T> setBody(Object body);
    IRequest<T> addMultipart(MultipartBody.Part part);
    IRequest<T> setBoundaryResultClass(Class<? extends IResult> boundaryResultClass);
    IRequest<T> baseUrl(String baseUrl);
    IRequest<T> addCallAdapterFactory(CallAdapter.Factory factory);

    IRequest<T> addConverterFactory(Converter.Factory factory);


    IRequest<T> addHeader(String key,String value);
    IRequest<T> https(SSLSocketFactory socketFactory, X509TrustManager trustManager);
    IRequest<T> hostnameVerifier(HostnameVerifier hostnameVerifier);
    IRequest<T> timeout(long connectTimeout,
                     long readTimeout,
                     long writeTimeout, TimeUnit timeUnit);
    IRequest<T> observeTimeout(long observeTimeout
                        , TimeUnit timeUnit);
    IRequest<T> observeTimeout(long observeTimeout);

    IRequest<T> retry(int retryCount,
                      long interval, Predicate<Throwable> predicate);
    IRequest<T> retry(int retryCount,
                      long interval);

    IRequest<T> observeOnMain();

    IRequest<T> observeOnMain(LifecycleOwner owner);

    Observable<T> toObservable();
    Observable<? extends IResult<T>> createObservable();

    void subscribe(Observer<T> observer);


    Retrofit getRetrofit();

    HashMap<String, String> getMergedHeaders();

    OkHttpClient getOkHttpClient();

    Class<? extends IResult> getBoundaryResultClass();

    int getRetryCount();

    long getRetryInterval();

    Type getResponseType();

    String getRelativeUrl();

    Object getPostBody();
    boolean isIResultResponse();

    IRequest<T> uploadFile(File file, ProgressListener progressListener);

    IRequest<T> uploadFile(ArrayList<File> files, ArrayList<ProgressListener> progressListeners);



}

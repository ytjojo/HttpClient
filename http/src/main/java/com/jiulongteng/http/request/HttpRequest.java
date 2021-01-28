package com.jiulongteng.http.request;

import androidx.lifecycle.LifecycleOwner;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.AbstractClient;
import com.jiulongteng.http.converter.GsonConverterFactory;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.exception.ExceptionHandle;
import com.jiulongteng.http.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;

public class HttpRequest<T> implements IRequest<T> {

    AbstractClient httpClient;
    Retrofit retrofit;
    private boolean isSync;
    HashMap<String, String> params;
    HashMap<String, String> headers;
    HashMap<String, Object> postParams;
    Object postBody;
    private HttpMethod httpMethod = HttpMethod.GET;

    OkHttpClient okHttpClient;

    long observeTimeout = -1;
    TimeUnit observeTimeUnit = TimeUnit.MILLISECONDS;
    Converter.Factory converterFactory;
    CallAdapter.Factory callAdapterFactory;

    boolean isRebuildRetrofit;
    String relativeUrl;
    Type responseType = Object.class;

    Observable observable;

    boolean isObservableMapped;

    int retryCount;
    int currentRetryCount;
    long retryInterval;
    private Predicate<Throwable> retryPredicate;

    @Override
    public IRequest<T> from(AbstractClient abstractClient, HttpMethod method) {
        this.httpClient = abstractClient;
        this.okHttpClient = abstractClient.getOkHttpClient();
        this.retrofit = abstractClient.getRetrofit();
        return this;
    }

    @Override
    public <T> IRequest<T> asType(TypeToken<T> typeToken) {
        responseType = typeToken.getType();
        Class rawType = $Gson$Types.getRawType(responseType);
        if (IResult.class.isAssignableFrom(rawType)) {
            isObservableMapped = true;
        }
        return (IRequest<T>) this;
    }

    @Override
    public IRequest<T> setSync() {
        isSync = true;
        return this;
    }

    @Override
    public IRequest<T> relativeUrl(String relativeUrl) {
        this.relativeUrl = relativeUrl;
        return this;
    }

    @Override
    public IRequest<T> add(String key, String value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return null;
    }

    @Override
    public IRequest<T> add(String key, Object value) {
        if (postParams == null) {
            postParams = new HashMap<>();
        }
        postParams.put(key, value);
        return null;
    }

    @Override
    public IRequest<T> add(Object body) {
        this.postBody = body;
        return null;
    }

    @Override
    public IRequest<T> baseUrl(String baseUrl) {
        retrofit = retrofit.newBuilder().baseUrl(baseUrl).build();
        return null;
    }

    @Override
    public IRequest<T> addCallAdapterFactory(CallAdapter.Factory factory) {
        this.callAdapterFactory = factory;
        retrofit = retrofit.newBuilder().addCallAdapterFactory(factory).build();
        return this;
    }

    @Override
    public IRequest<T> addConverterFactory(Converter.Factory factory) {
        this.converterFactory = factory;
        isRebuildRetrofit = true;
        return this;
    }

    @Override
    public IRequest<T> addHeader(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(key, value);
        return this;
    }

    @Override
    public IRequest<T> https(SSLSocketFactory socketFactory, X509TrustManager trustManager) {
        okHttpClient = okHttpClient.newBuilder().sslSocketFactory(socketFactory, trustManager).build();
        retrofit = retrofit.newBuilder().client(okHttpClient).build();
        return this;
    }

    @Override
    public IRequest<T> hostnameVerifier(HostnameVerifier hostnameVerifier) {
        okHttpClient = okHttpClient.newBuilder().hostnameVerifier(hostnameVerifier).build();
        retrofit = retrofit.newBuilder().client(okHttpClient).build();
        return this;
    }

    @Override
    public IRequest<T> timeout(long connectTimeout, long readTimeout, long writeTimeout, TimeUnit timeUnit) {
        okHttpClient = okHttpClient.newBuilder().connectTimeout(connectTimeout, timeUnit)
                .readTimeout(readTimeout, timeUnit)
                .writeTimeout(writeTimeout, timeUnit)
                .build();
        retrofit = retrofit.newBuilder().client(okHttpClient).build();
        return this;
    }

    @Override
    public IRequest<T> observeTimeout(long observeTimeout, TimeUnit timeUnit) {
        this.observeTimeout = observeTimeout;
        this.observeTimeUnit = timeUnit;
        return this;
    }

    @Override
    public IRequest<T> observeTimeout(long observeTimeout) {
        this.observeTimeout = observeTimeout;
        return this;
    }

    @Override
    public IRequest<T> retry(int retryCount, long interval, Predicate<Throwable> predicate) {
        this.retryCount = retryCount;
        this.retryInterval = interval;
        this.retryPredicate = predicate;
        return this;
    }

    @Override
    public IRequest<T> retry(int retryCount, long interval) {
        this.retryCount = retryCount;
        this.retryInterval = interval;
        return this;
    }

    @Override
    public IRequest<T> observeOnMain() {
        createObservable();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        return this;
    }

    @Override
    public IRequest<T> observeOnMain(LifecycleOwner owner) {
        createObservable();
        observable = observable.observeOn(AndroidSchedulers.mainThread())
                .flatMap(httpClient.flatmap(owner));
        return this;
    }

    @Override
    public Retrofit getRetrofit() {
        createRetrofit();
        isRebuildRetrofit = false;
        return retrofit;
    }

    public <U> U getService(Class<U> serviceClass) {
        return (U) getRetrofit().create(serviceClass);
    }

    public HashMap<String, String> getMergedHeaders() {
        if (headers == null) {
            headers = new HashMap<>();
        }
        HashMap<String, String> addHeaders = new HashMap<>();
        addHeaders.putAll(httpClient.getHeaders());
        addHeaders.putAll(headers);
        return addHeaders;
    }

    @Override
    public Observable<T> toObservable() {
        createObservable();
        if (!isObservableMapped) {
            isObservableMapped = true;
            return observable = observable.flatMap(new Function<IResult<T>, ObservableSource<T>>() {
                @Override
                public ObservableSource<T> apply(IResult<T> tiResult) throws Throwable {
                    if (responseType == Object.class) {
                        return (ObservableSource<T>) Observable.just(new Object());
                    }
                    return Observable.just(tiResult.getData());
                }
            });
        }
        transformObservable();
        return observable;

    }

    @Override
    public Observable<? extends IResult<T>> createObservable() {
        if (observable != null) {
            return observable;
        }
        switch (httpMethod) {
            case GET:
                observable = getService(AbstractClient.Service.class).
                        get(getMergedHeaders(), AbstractClient.getUrl(
                                retrofit.baseUrl().toString(), relativeUrl, params))
                        .map(httpClient.map(responseType));


                break;
            case POST:
                Observable<Response<ResponseBody>> observableResponse = null;
                if (postBody == null && CollectionUtils.isEmpty(params) && CollectionUtils.isEmpty(postParams)) {
                    observableResponse = getService(AbstractClient.Service.class).post(getMergedHeaders(), relativeUrl);
                } else {
                    if (postBody != null) {
                        observableResponse = getService(AbstractClient.Service.class).post(getMergedHeaders(), relativeUrl, postBody);
                    } else {
                        if (postParams == null) {
                            postParams = new HashMap<>();
                        }
                        if (params != null) {
                            postParams.putAll(params);
                        }
                        observableResponse = getService(AbstractClient.Service.class).post(getMergedHeaders(), relativeUrl, postParams);
                    }
                }
                observable = observableResponse.map(httpClient.map(responseType));


                break;
            case POSTFORM:

                break;
            case DELETE:
                break;
            case PUT:
                break;
            default:
                break;
        }
        if (!isSync) {
            observable = observable.subscribeOn(Schedulers.io());
        }
        return observable;
    }

    @Override
    public HttpRequest<IResult<T>> asResult() {
        isObservableMapped = false;
        return (HttpRequest<IResult<T>>) this;
    }


    @Override
    public void subscribe(Observer<T> observer) {
        createObservable();
        transformObservable();
        observable.subscribe(observer);
    }

    public void transformObservable() {
        if (!isObservableMapped) {
            observable = toObservable();
        }
        if (observeTimeout > 0) {
            observable = observable.timeout(observeTimeout, observeTimeUnit);
        }
        observable = observable.retryWhen(new Function<Observable<Throwable>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Throwable> throwableObservable) throws Throwable {
                return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {

                    @Override
                    public ObservableSource<?> apply(Throwable throwable) throws Throwable {
                        currentRetryCount++;
                        if (currentRetryCount > retryCount) {
                            return Observable.error(ExceptionHandle.handleException(throwable));
                        } else {
                            if (retryPredicate != null) {
                                if (retryPredicate.test(throwable)) {
                                    return Observable.timer(retryInterval, TimeUnit.MILLISECONDS);
                                } else {
                                    return Observable.error(ExceptionHandle.handleException(throwable));
                                }
                            } else {
                                return Observable.timer(retryInterval, TimeUnit.MILLISECONDS);
                            }

                        }

                    }
                });
            }
        });
    }

    private void createRetrofit() {
        if (!isRebuildRetrofit) {
            return;
        }
        if (callAdapterFactory == null) {
            callAdapterFactory = RxJava3CallAdapterFactory.create();
        }
        if (converterFactory == null) {
            converterFactory = GsonConverterFactory.create();
        }
        retrofit = new Retrofit.Builder().baseUrl(retrofit.baseUrl())
                .addCallAdapterFactory(callAdapterFactory)
                .client(okHttpClient)
                .addConverterFactory(converterFactory).build();
    }

    public static enum HttpMethod {
        GET, POST, POSTFORM, DELETE, PUT
    }
}

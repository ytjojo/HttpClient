package com.jiulongteng.http.request;

import androidx.lifecycle.LifecycleOwner;

import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.AbstractClient;
import com.jiulongteng.http.converter.GsonConverterFactory;
import com.jiulongteng.http.converter.GsonResponseBodyConverter;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.entities.StandardResult;
import com.jiulongteng.http.exception.ExceptionHandle;
import com.jiulongteng.http.progress.ProgressListener;
import com.jiulongteng.http.progress.ProgressRequestBody;
import com.jiulongteng.http.util.CollectionUtils;
import com.jiulongteng.http.util.LogUtil;
import com.jiulongteng.http.util.TextUtils;
import com.jiulongteng.http.util.TypeUtil;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
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
    HashMap<String, Object> params;
    HashMap<String, String> headers;
    ArrayList<MultipartBody.Part> multiparts;
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

    boolean isIResultResponse;

    int retryCount;
    int currentRetryCount;
    long retryInterval;

    private boolean isMultipart;
    private Predicate<Throwable> retryPredicate;

    private Class<? extends IResult> boundaryResultClass;

    @Override
    public IRequest<T> from(AbstractClient abstractClient, HttpMethod method) {
        this.httpClient = abstractClient;
        this.okHttpClient = abstractClient.getOkHttpClient();
        this.retrofit = abstractClient.getRetrofit();
        this.boundaryResultClass = abstractClient.getBoundaryResultClass();
        return this;
    }

    @Override
    public <U> IRequest<U> asType(TypeToken<U> typeToken) {
        responseType = typeToken.getType();
        if (this.responseType instanceof ParameterizedType && IResult.class.isAssignableFrom((Class) ((ParameterizedType) this.responseType).getRawType())) {
            isIResultResponse = true;
        }
        return (IRequest<U>) this;
    }

    @Override
    public <U> IRequest<U> asType(Type type) {
        responseType = type;
        if (this.responseType instanceof ParameterizedType && IResult.class.isAssignableFrom((Class) ((ParameterizedType) this.responseType).getRawType())) {
            isIResultResponse = true;
        }
        return (IRequest<U>) this;
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
    public IRequest<T> formatUrlParams(Object... params) {
        this.relativeUrl = String.format(relativeUrl, params);
        return this;
    }

    @Override
    public IRequest<T> add(String key, Object value) {
        if (params == null) {
            params = new HashMap<>();
        }
        params.put(key, value);
        return this;
    }


    @Override
    public IRequest<T> setBody(Object body) {
        this.postBody = body;
        return this;
    }

    @Override
    public IRequest<T> addMultipart(MultipartBody.Part part) {
        if (multiparts == null) {
            multiparts = new ArrayList<>();
        }
        multiparts.add(part);
        isMultipart = true;
        return this;
    }

    @Override
    public IRequest<T> uploadFile(File file, ProgressListener progressListener) {
        String fileName = file.getName();
        // 通过文件名获取文件类型
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(fileName);
        if (TextUtils.isEmpty(contentType)) {
            // 使用通用文件类型 xxx.*
            contentType = "application/octet-stream";
        }
        // 第一个参数为文件的 key, 即 partName,需要和服务器约定
        // 对应的请求头: Content-Disposition: form-data; name="file$i"; filename="xxx"
        addMultipart(MultipartBody.Part.createFormData("file", fileName,
                new ProgressRequestBody(
                        RequestBody.create(MediaType.parse(contentType), file),
                        progressListener)));
        return this;
    }

    @Override
    public IRequest<T> uploadFile(ArrayList<File> files, ArrayList<ProgressListener> progressListeners) {
        int index = 0;
        for (File file : files) {
            ProgressListener progressListener = null;
            if (progressListeners != null && index < progressListeners.size()) {
                progressListener = progressListeners.get(index);
            }
            uploadFile(file, progressListener);
            index++;
        }
        return this;
    }


    @Override
    public IRequest<T> setBoundaryResultClass(Class<? extends IResult> boundaryResultClass) {
        this.boundaryResultClass = boundaryResultClass;
        return this;
    }

    @Override
    public IRequest<T> baseUrl(String baseUrl) {
        retrofit = retrofit.newBuilder().baseUrl(baseUrl).build();
        return this;
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

    @Override
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
        transformObservable();
        return observable;

    }

    @Override
    public Observable<? extends IResult<T>> createObservable() {
        if (observable != null) {
            return observable;
        }
        if (params == null) {
            params = new HashMap<>();
        }
        switch (httpMethod) {
            case GET:

                HashMap<String, String> querys = new HashMap<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    querys.put(entry.getKey(), entry.getValue().toString());
                }
                observable = getService(AbstractClient.Service.class).
                        get(getMergedHeaders(), AbstractClient.getUrl(
                                retrofit.baseUrl().toString(), relativeUrl, querys))
                        .map(httpClient.map(getRetrofit(), responseType, boundaryResultClass));


                break;
            case POST:
                Observable<Response<ResponseBody>> observableResponse = null;
                Object bodyObject = getPostBody();
                if (bodyObject == null) {
                    observableResponse = getService(AbstractClient.Service.class).post(getMergedHeaders(), relativeUrl);
                } else {
                    observableResponse = getService(AbstractClient.Service.class).post(getMergedHeaders(), relativeUrl, bodyObject);

                }
                observable = observableResponse.map(httpClient.map(getRetrofit(), responseType, boundaryResultClass));


                break;
            case POSTFORM:

                if (isMultipart) {
                    if (!CollectionUtils.isEmpty(params)) {
                        if (multiparts == null) {
                            multiparts = new ArrayList<>();
                        }
                        for (Map.Entry<String, Object> entry : params.entrySet()) {
                            MultipartBody.Part part = MultipartBody.Part.createFormData(entry.getKey(), entry.getValue().toString());
                            multiparts.add(part);
                        }
                    }
                    getService(AbstractClient.Service.class).multipartPost(getMergedHeaders(), relativeUrl, multiparts);
                } else {
                    HashMap<String, String> fieldMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        fieldMap.put(entry.getKey(), entry.getValue().toString());
                    }
                    getService(AbstractClient.Service.class).postFormUrlEncoded(getMergedHeaders(), relativeUrl, fieldMap);
                }


                break;
            case DELETE:
                querys = new HashMap<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    querys.put(entry.getKey(), entry.getValue().toString());
                }
                observable = getService(AbstractClient.Service.class).
                        delete(getMergedHeaders(), AbstractClient.getUrl(
                                retrofit.baseUrl().toString(), relativeUrl, querys))
                        .map(httpClient.map(getRetrofit(), responseType, boundaryResultClass));
                break;
            case PUT:
                if (isMultipart) {
                    if (!CollectionUtils.isEmpty(params)) {
                        if (multiparts == null) {
                            multiparts = new ArrayList<>();
                        }
                        for (Map.Entry<String, Object> entry : params.entrySet()) {
                            MultipartBody.Part part = MultipartBody.Part.createFormData(entry.getKey(), entry.getValue().toString());
                            multiparts.add(part);
                        }
                    }
                    getService(AbstractClient.Service.class).put(getMergedHeaders(), relativeUrl, multiparts);
                } else {
                    HashMap<String, String> fieldMap = new HashMap<>();
                    for (Map.Entry<String, Object> entry : params.entrySet()) {
                        fieldMap.put(entry.getKey(), entry.getValue().toString());
                    }
                    observable = getService(AbstractClient.Service.class).
                            put(getMergedHeaders(), relativeUrl, fieldMap)
                            .map(httpClient.map(getRetrofit(), responseType, boundaryResultClass));
                }

                break;
            case HEAD:
                querys = new HashMap<>();
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    querys.put(entry.getKey(), entry.getValue().toString());
                }
                observable = getService(AbstractClient.Service.class).
                        head(getMergedHeaders(), AbstractClient.getUrl(
                                retrofit.baseUrl().toString(), relativeUrl, querys))
                        .map(httpClient.map(getRetrofit(), responseType, boundaryResultClass));


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
        isIResultResponse = true;
        return (HttpRequest<IResult<T>>) this;
    }


    @Override
    public void subscribe(Observer<T> observer) {
        createObservable();
        transformObservable();
        observable.subscribe(observer);
    }

    public void transformObservable() {


        if (!isIResultResponse) {
            observable = observable.flatMap(new Function<IResult<T>, ObservableSource<T>>() {
                @Override
                public ObservableSource<T> apply(IResult<T> tiResult) throws Throwable {
                    if (responseType == Object.class) {
                        return (ObservableSource<T>) Observable.just(new Object());
                    }
                    return Observable.just(tiResult.getData());
                }
            });
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
                                if (retryInterval > 0) {
                                    return Observable.timer(retryInterval, TimeUnit.MILLISECONDS);
                                } else {
                                    return Observable.just("");
                                }

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
        GET, POST, POSTFORM, DELETE, PUT, HEAD
    }

    @Override
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    @Override
    public Class<? extends IResult> getBoundaryResultClass() {
        return boundaryResultClass;
    }

    @Override
    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public long getRetryInterval() {
        return retryInterval;
    }

    @Override
    public Type getResponseType() {
        return responseType;
    }

    @Override
    public String getRelativeUrl() {
        return relativeUrl;
    }

    @Override
    public Object getPostBody() {
        if (postBody != null) {
            return postBody;
        }
        if (params != null) {
            if (params == null) {
                params = new HashMap<>();
            }
            params.putAll(params);
        }
        postBody = params;
        return postBody;
    }

    @Override
    public boolean isIResultResponse() {
        return isIResultResponse;
    }

    public static IResult convertToIResult(IRequest request, Response<ResponseBody> rawResponse) throws IOException {
        LogUtil.logThread("convertToIResult");
        Type fixType = request.getResponseType();

        if (request.getBoundaryResultClass() != null && !TypeUtil.isAssignableFrom(request.getBoundaryResultClass(), request.getResponseType())) {
            fixType = $Gson$Types.newParameterizedTypeWithOwner(null, request.getBoundaryResultClass(), new Type[]{request.getResponseType()});
        }

        Converter<ResponseBody, IResult> convert = request.getRetrofit().responseBodyConverter(fixType, new Annotation[0]);
        if (convert instanceof GsonResponseBodyConverter) {
            GsonResponseBodyConverter gsonConverter = (GsonResponseBodyConverter) convert;
            gsonConverter.setBoundaryResultClass(request.getBoundaryResultClass());
        }
        Object data = convert.convert(rawResponse.body());
        if (request.getBoundaryResultClass() != null && TypeUtil.isAssignableFrom(IResult.class, request.getBoundaryResultClass())) {
            ((IResult) data).setHeaders(rawResponse.headers());
            return (IResult) data;
        }
        StandardResult standardResult = new StandardResult();
        standardResult.data = data;
        standardResult.setHeaders(rawResponse.headers());
        standardResult.code = 0;
        return standardResult;
    }
}

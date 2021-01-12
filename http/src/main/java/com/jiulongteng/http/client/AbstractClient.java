package com.jiulongteng.http.client;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.jiulongteng.http.annotation.RawString;
import com.jiulongteng.http.callback.HttpCallback;
import com.jiulongteng.http.converter.GsonConverterFactory;
import com.jiulongteng.http.entities.StandardResult;
import com.jiulongteng.http.https.UnSafeHostnameVerifier;
import com.jiulongteng.http.interceptor.HttpLoggingInterceptor;
import com.jiulongteng.http.interceptor.InvocationLogger;
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public abstract class AbstractClient {
    private String baseUrl;

    IHttpClientFactory iHttpClientFactory;

    private OkHttpClient okHttpClient;

    private Retrofit retrofit;

    private Object tag;

    public AbstractClient() {
    }

    public AbstractClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void attachToFactory(IHttpClientFactory httpClientFactory) {
        this.iHttpClientFactory = httpClientFactory;
    }

    public Retrofit createRetrofit() {
        Retrofit.Builder builder = (new Retrofit.Builder()).baseUrl(this.baseUrl).addConverterFactory(getConverterFactory()).client(getOkhttpClient(this.iHttpClientFactory));
        CallAdapter.Factory factory = getCallAdapterFactory();
        if (factory != null)
            builder.addCallAdapterFactory(factory);
        Retrofit retrofit = builder.build();
        this.retrofit = retrofit;
        return retrofit;
    }

    public Function<Response<ResponseBody>, ObservableSource<Response<ResponseBody>>> flatmap(final LifecycleOwner owner) {
        return new Function<Response<ResponseBody>, ObservableSource<Response<ResponseBody>>>() {
            public ObservableSource<Response<ResponseBody>> apply(Response<ResponseBody> param1Response) throws Throwable {
                Observable observable = Observable.just(param1Response);
                LifecycleOwner lifecycleOwner = owner;
                if (lifecycleOwner != null)
                    observable = observable.compose((ObservableTransformer) AndroidLifecycle.createLifecycleProvider(lifecycleOwner).bindUntilEvent(Lifecycle.Event.ON_DESTROY));
                return (ObservableSource<Response<ResponseBody>>) observable;
            }
        };
    }

    public <T> void get(@Nullable LifecycleOwner lifecycleOwner, String url, HttpCallback<T> httpCallback) {
        get(lifecycleOwner, url, null, httpCallback);
    }

    public <T> void get(@Nullable LifecycleOwner lifecycleOwner, String url, @Nullable String[] params, HttpCallback<T> httpCallback) {

        url = getUrl(url, params);
        ((Service) getService(Service.class)).get(getHeaders(), url).flatMap(flatmap(lifecycleOwner)).map(map(httpCallback)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((Observer) httpCallback);
    }

    public <T> void get(String url, HttpCallback<T> httpCallback) {
        get(null, url, null, httpCallback);
    }

    public <T> void get(String url, @Nullable String[] params, HttpCallback<T> httpCallback) {
        get(null, url, null, httpCallback);
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public CallAdapter.Factory getCallAdapterFactory() {
        return (CallAdapter.Factory) RxJava3CallAdapterFactory.create();
    }

    public Converter.Factory getConverterFactory() {
        return (Converter.Factory) GsonConverterFactory.create();
    }

    public HashMap<String, String> getHeaders() {
        return new HashMap<String, String>();
    }

    public ArrayList<Interceptor> getInterceptors() {
        return new ArrayList<Interceptor>();
    }

    public OkHttpClient getOkHttpClient() {
        return this.okHttpClient;
    }

    public OkHttpClient getOkhttpClient(IHttpClientFactory iHttpClientFactory) {
        OkHttpClient.Builder builder = iHttpClientFactory.getBaseOkHttpClient().newBuilder();
        ArrayList<Interceptor> arrayList = getInterceptors();
        if (arrayList != null && !arrayList.isEmpty()) {
            Iterator<Interceptor> iterator = arrayList.iterator();
            while (iterator.hasNext())
                builder.addInterceptor(iterator.next());
        }

        if (isShowLog() && iHttpClientFactory.isShowLog()) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                public void log(String param1String) {

                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor((Interceptor) httpLoggingInterceptor);
            builder.addInterceptor(new InvocationLogger());
        }
        SSLSocketFactory sslSocketFactory = getSslSocketFactory();
        X509TrustManager trustManager = getTrustManager();
        if (sslSocketFactory != null && trustManager != null) {

            builder.sslSocketFactory(sslSocketFactory, trustManager);
        }
        if (getHttpCache() != null) {
            Cache cache = new Cache(getHttpCache(), getMaxCacheSize());
            builder.cache(cache);
        }

        OkHttpClient okHttpClient = builder.build();
        this.okHttpClient = okHttpClient;
        return okHttpClient;
    }

    protected boolean isShowLog() {
        return false;
    }

    public abstract File getHttpCache();

    public abstract long getMaxCacheSize();

    public OkHttpClient.Builder configOkHttpClient(OkHttpClient.Builder builder) {
        return builder;
    }


    public Retrofit getRetrofit() {
        if (this.retrofit == null)
            this.retrofit = createRetrofit();
        return this.retrofit;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return null;
    }

    public X509TrustManager getTrustManager() {
        return null;
    }

    public HostnameVerifier getHostnameVerifier() {
        return new UnSafeHostnameVerifier();
    }

    public <T> T getService(Class<T> serviceClass) {
        return (T) getRetrofit().create(serviceClass);
    }

    public Object getTag() {
        return this.tag;
    }

    public String getUrl(String url, String[] params) {
        Uri.Builder uriBuilder = null;
        if (!TextUtils.isEmpty(getBaseUrl())) {
            uriBuilder = Uri.parse(baseUrl).buildUpon().appendPath(url);
        } else {
            uriBuilder = Uri.parse(url).buildUpon().appendPath(url);
        }
        if (params == null || params.length == 0) {
            return uriBuilder.build().toString();
        } else if (params.length % 2 == 0) {
            for (byte i = 0; i < params.length; i += 2) {
                uriBuilder.appendQueryParameter(params[i], params[i + 1]);
            }
            return uriBuilder.build().toString();
        }
        throw new IllegalArgumentException("数组 querys 长度必须为偶数");
    }

    public <T> Function<Response<ResponseBody>, StandardResult<T>> map(final HttpCallback<T> callback) {
        return new Function<Response<ResponseBody>, StandardResult<T>>() {
            public StandardResult<T> apply(Response<ResponseBody> param1Response) throws Throwable {
                T data = (T) getRetrofit().responseBodyConverter(callback.getResultType(), new Annotation[0]).convert(param1Response.body());
                StandardResult<T> standardResult = new StandardResult();
                standardResult.data = data;
                standardResult.headers = param1Response.headers();
                standardResult.code = 0;
                return standardResult;
            }
        };
    }

    public <T> void post(@Nullable LifecycleOwner lifecycleOwner, String url, @Nullable Object body, HttpCallback<T> httpCallback
    ) {
        Observable<Response<ResponseBody>> observable;
        if (getBaseUrl() == null)
            return;
        url = getUrl(url, null);
        if (body == null) {
            observable = ((Service) getService(Service.class)).post(getHeaders(), url);
        } else {
            observable = ((Service) getService(Service.class)).post(getHeaders(), url, body);
        }
        observable.flatMap(flatmap(lifecycleOwner)).map(map(httpCallback)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe((Observer) httpCallback);
    }

    public void setTag(Object paramObject) {
        this.tag = paramObject;
    }

    public static interface Service {
        @RawString
        @GET
        Observable<Response<ResponseBody>> get(@HeaderMap Map<String, String> params, @Url String url);

        @RawString
        @Multipart
        @POST
        Observable<Response<ResponseBody>> multipartPost(@HeaderMap Map<String, String> params, @Url String url, @Part List<MultipartBody.Part> partList);

        @RawString
        @POST
        Observable<Response<ResponseBody>> post(@HeaderMap Map<String, String> params, @Url String url);

        @RawString
        @POST
        Observable<Response<ResponseBody>> post(@HeaderMap Map<String, String> params, @Url String url, @Body Object body);

        @POST
        Observable<Response<ResponseBody>> upload(@HeaderMap Map<String, String> params, @Url String url, @Body MultipartBody multipartBody);
    }
}


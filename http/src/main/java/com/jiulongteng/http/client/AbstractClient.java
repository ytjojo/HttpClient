package com.jiulongteng.http.client;

import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.google.gson.internal.$Gson$Types;
import com.jiulongteng.http.annotation.RawString;
import com.jiulongteng.http.callback.HttpCallback;
import com.jiulongteng.http.converter.GsonConverterFactory;
import com.jiulongteng.http.converter.GsonResponseBodyConverter;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.entities.StandardResult;
import com.jiulongteng.http.https.UnSafeHostnameVerifier;
import com.jiulongteng.http.interceptor.HttpLoggingInterceptor;
import com.jiulongteng.http.interceptor.InvocationLogger;
import com.jiulongteng.http.request.HttpRequest;
import com.jiulongteng.http.request.IRequest;
import com.jiulongteng.http.util.TypeUtil;
import com.trello.lifecycle4.android.lifecycle.AndroidLifecycle;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
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
import okhttp3.HttpUrl;
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
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.HTTP;
import retrofit2.http.HeaderMap;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Url;

public abstract class AbstractClient {

    IHttpClientFactory iHttpClientFactory;

    private OkHttpClient okHttpClient;

    private Retrofit retrofit;

    private Object tag;
    String mBaseUrl;

    private Class<? extends IResult> boundaryResultClass = StandardResult.class;

    public AbstractClient() {

    }

    public AbstractClient(String baseUrl) {
        this.mBaseUrl = baseUrl;
    }

    public void attachToFactory(IHttpClientFactory httpClientFactory) {
        this.iHttpClientFactory = httpClientFactory;
    }

    public IHttpClientFactory getHttpClientFactory() {
        return iHttpClientFactory;
    }

    public Retrofit createRetrofit() {
        Retrofit.Builder builder = (new Retrofit.Builder()).
                addConverterFactory(getConverterFactory())
                .client(createOkhttpClient(this.iHttpClientFactory));
        if (getBaseUrl() != null) {
            builder.baseUrl(this.getBaseUrl());
        }
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


    public String getBaseUrl() {
        return this.mBaseUrl.toString();
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


    public OkHttpClient createOkhttpClient(IHttpClientFactory iHttpClientFactory) {
        OkHttpClient.Builder builder = iHttpClientFactory.getBaseOkHttpClient().newBuilder();
        ArrayList<Interceptor> arrayList = getInterceptors();
        if (arrayList != null && !arrayList.isEmpty()) {
            Iterator<Interceptor> iterator = arrayList.iterator();
            while (iterator.hasNext()) {
                builder.addInterceptor(iterator.next());
            }

        }

        if (isShowLog() && iHttpClientFactory.isShowLog()) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                public void log(String log) {
                    System.out.println(log);
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
        builder.hostnameVerifier(getHostnameVerifier());
        if (iHttpClientFactory.getHttpCacheParent() != null) {
            Cache cache = new Cache(getHttpCache(), getMaxCacheSize());
            builder.cache(cache);
        }

        OkHttpClient okHttpClient = builder.build();
        this.okHttpClient = okHttpClient;
        return okHttpClient;
    }

    public Class<? extends IResult> getBoundaryResultClass() {
        return boundaryResultClass;
    }

    public void setBoundaryResultClass(Class<? extends IResult> boundaryResultClass) {
        this.boundaryResultClass = boundaryResultClass;
    }

    public OkHttpClient getOkHttpClient() {
        if (this.okHttpClient == null) {
            createOkhttpClient(iHttpClientFactory);
        }
        return okHttpClient;

    }


    protected boolean isShowLog() {
        return true;
    }

    public File getHttpCache() {
        return new File(getHttpClientFactory().getHttpCacheParent(), "http");
    }

    public long getMaxCacheSize() {
        return 50 * 1000_000;
    }

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


    public static HashMap<String, String> array2Map(String[] params) {
        if (params == null || params.length == 0) {
            return new HashMap<>();
        } else if (params.length % 2 == 0) {
            HashMap<String, String> paramsMap = new HashMap<>();
            for (int i = 0; i < params.length; i += 2) {
                paramsMap.put(params[i], params[i + 1]);
            }
            return paramsMap;
        }
        return new HashMap<String, String>();
    }

    public static String getUrl(String baseUrl, String relativeUrl, HashMap<String, String> params) {
        HttpUrl.Builder uriBuilder = baseUrl == null || relativeUrl.startsWith("http://") || relativeUrl.startsWith("https://") ? HttpUrl.get(relativeUrl).newBuilder() : HttpUrl.get(baseUrl).newBuilder(relativeUrl);

        if (params == null || params.size() == 0) {
            return uriBuilder.build().toString();
        } else {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                uriBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
            return uriBuilder.build().toString();
        }
    }


    public void setTag(Object tag) {
        this.tag = tag;
    }

    public static <T> Function<Response<ResponseBody>, IResult<T>> map(Retrofit retrofit, Type type, Class<? extends IResult> resultBoundary) {
        return new Function<Response<ResponseBody>, IResult<T>>() {
            public IResult<T> apply(Response<ResponseBody> rawResponse) throws Throwable {
                Type fixType = type;

                if (resultBoundary != null && !TypeUtil.isAssignableFrom(resultBoundary, type)) {
                    fixType = $Gson$Types.newParameterizedTypeWithOwner(null, resultBoundary, new Type[]{type});
                }

                Converter<ResponseBody, T> convert = retrofit.responseBodyConverter(fixType, new Annotation[0]);
                if (convert instanceof GsonResponseBodyConverter) {
                    GsonResponseBodyConverter gsonConverter = (GsonResponseBodyConverter) convert;
                    gsonConverter.setBoundaryResultClass(resultBoundary);
                }
                T data = (T) convert.convert(rawResponse.body());
                if (resultBoundary != null && TypeUtil.isAssignableFrom(IResult.class,resultBoundary)) {
                    ((IResult) data).setHeaders(rawResponse.headers());
                    return (IResult) data;
                }
                StandardResult<T> standardResult = new StandardResult();
                standardResult.data = data;
                standardResult.setHeaders(rawResponse.headers());
                standardResult.code = 0;
                return standardResult;
            }
        };
    }

    public <T> void post(@Nullable LifecycleOwner lifecycleOwner, String relativeUrl, @Nullable Object body, HttpCallback<T> httpCallback
    ) {
        Observable<Response<ResponseBody>> observable;

        if (body == null) {
            observable = ((Service) getService(Service.class)).post(getHeaders(), relativeUrl);
        } else {
            observable = ((Service) getService(Service.class)).post(getHeaders(), relativeUrl, body);
        }
        observable.
                flatMap(flatmap(lifecycleOwner))
                .map(map(getRetrofit(), httpCallback.getResultType(), getBoundaryResultClass())).
                subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe((Observer) httpCallback);
    }


    public <T> void get(@Nullable LifecycleOwner lifecycleOwner, String relativeUrl, HttpCallback<T> httpCallback) {
        get(lifecycleOwner, relativeUrl, null, httpCallback);
    }

    public <T> void get(@Nullable LifecycleOwner lifecycleOwner, String relativeUrl, @Nullable String[] params, HttpCallback<T> httpCallback) {

        String url = getUrl(getBaseUrl(), relativeUrl, array2Map(params));
        ((Service) getService(Service.class)).get(getHeaders(), url).
                flatMap(flatmap(lifecycleOwner)).
                map(map(getRetrofit(), httpCallback.getResultType(), getBoundaryResultClass())).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                subscribe((Observer) httpCallback);
    }

    public <T> void get(String relativeUrl, HttpCallback<T> httpCallback) {
        get(null, relativeUrl, null, httpCallback);
    }

    public <T> void get(String relativeUrl, @Nullable String[] params, HttpCallback<T> httpCallback) {
        get(null, relativeUrl, params, httpCallback);
    }


    public static interface Service {
        @RawString
        @GET
        Observable<Response<ResponseBody>> get(@HeaderMap Map<String, String> headers, @Url String url);

        @RawString
        @Multipart
        @POST
        Observable<Response<ResponseBody>> multipartPost(@HeaderMap Map<String, String> headers, @Url String url, @Part List<MultipartBody.Part> partList);

        @RawString
        @FormUrlEncoded
        @POST
        Observable<Response<ResponseBody>> postFormUrlEncoded(@HeaderMap Map<String, String> headers, @Url String url,@FieldMap HashMap<String,String> fieldMap);

        @RawString
        @POST
        Observable<Response<ResponseBody>> post(@HeaderMap Map<String, String> headers, @Url String url);

        @RawString
        @POST
        Observable<Response<ResponseBody>> post(@HeaderMap Map<String, String> headers, @Url String url, @Body Object body);

        @POST
        @RawString
        Observable<Response<ResponseBody>> upload(@HeaderMap Map<String, String> headers, @Url String url, @Body MultipartBody multipartBody);

        @HEAD
        @RawString
        Observable<Response<ResponseBody>> head(@HeaderMap Map<String,String> headers,String url);

        @HTTP(method = "DELETE", hasBody= true)
        @RawString
        Observable<Response<ResponseBody>> delete(@HeaderMap Map<String,String> headers,String url);

        @RawString
        @PUT
        @FormUrlEncoded
        Observable<Response<ResponseBody>> put(@HeaderMap Map<String, String> headers, @Url String url, @FieldMap HashMap<String,String> fieldMap);

        @RawString
        @PUT
        @Multipart
        Observable<Response<ResponseBody>> put(@HeaderMap Map<String, String> headers, @Url String url, @Part List<MultipartBody.Part> partList);



    }


    public IRequest<Object> get(String relativeUrl) {
        return new HttpRequest<Object>().from(this, HttpRequest.HttpMethod.GET).relativeUrl(relativeUrl);
    }

    public IRequest<Object> post(String relativeUrl) {
        return new HttpRequest<Object>().from(this, HttpRequest.HttpMethod.POST).relativeUrl(relativeUrl);
    }

    public IRequest<Object> postForm(String relativeUrl) {
        return new HttpRequest<Object>().from(this, HttpRequest.HttpMethod.POSTFORM).relativeUrl(relativeUrl);
    }

    public IRequest<Object> head(String relativeUrl) {
        return new HttpRequest<Object>().from(this, HttpRequest.HttpMethod.HEAD).relativeUrl(relativeUrl);
    }

    public IRequest<Object> put(String relativeUrl) {
        return new HttpRequest<Object>().from(this, HttpRequest.HttpMethod.PUT).relativeUrl(relativeUrl);
    }
    public IRequest<Object> delete(String relativeUrl) {
        return new HttpRequest<Object>().from(this, HttpRequest.HttpMethod.DELETE).relativeUrl(relativeUrl);
    }

    public IRequest<Object> request(String relativeUrl,HttpRequest.HttpMethod method) {
        return new HttpRequest<Object>().from(this, method).relativeUrl(relativeUrl);
    }
}


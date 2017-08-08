package com.ytjojo.http;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.ytjojo.http.coverter.GsonConverterFactory;
import com.ytjojo.http.https.HttpsDelegate;
import com.ytjojo.http.https.UnSafeHostnameVerifier;
import com.ytjojo.http.interceptor.HeaderCallable;
import com.ytjojo.http.interceptor.HeaderInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.ProxyHandler;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class RetrofitClient {
    public static volatile  String TOKEN;
    public static  final String TOKEN_HEADER_KEY = "X-Access-Token";
    public static  final String ContentType_JSON = "application/json";
    public static  final String ContentType_FORM = "application/x-www-form-urlencoded; charset=UTF-8";
    private Retrofit retrofit ;
    static OkHttpClient mOkHttpClient;
    public RetrofitClient(Retrofit retrofit){
        this.retrofit = retrofit;
    }
    public RetrofitClient(Retrofit retrofit,HeaderInterceptor headerInterceptor){
        this.mHeaderInterceptor = headerInterceptor;
        this.retrofit = retrofit;
    }
    public static RetrofitClient mDefaultRetrofitClient;
    private HeaderInterceptor mHeaderInterceptor;
    public void putHeader(String key,String value){
        mHeaderInterceptor.putHeader(key,value);
    }

    /**
     * 用户在登录页面登录成功之后
     * 获得token 要调用此方法
     * 方法会重置自动刷新token的标识
     * @param key
     * @param token
     */
    public void onUserLoginGetToke(String key,String token){
        mHeaderInterceptor.onUserLoginGetToken(key,token);
    }

    public void clearCached(){
        try {
            mOkHttpClient.cache().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void init(@Nullable Context c,String baseUrl){
        mDefaultRetrofitClient = RetrofitClient.newBuilder(c).unsafeSSLSocketFactory().baseUrl(baseUrl).build();
    }
    public static void init(Builder builder){
        mDefaultRetrofitClient = builder.unsafeSSLSocketFactory().build();
    }
    public static void setDefault(RetrofitClient client){
        mDefaultRetrofitClient = client;
    }
    public void addInterceptor(Interceptor interceptor){
        mOkHttpClient.interceptors().add(interceptor);
    }
    public void removeInterceptor(Interceptor interceptor){
        mOkHttpClient.interceptors().remove(interceptor);
    }
    public static Builder newBuilder(@Nullable  Context context){
        return new Builder(context);
    }
    public static class Builder{
        Context context;
        String baseUrl;
        HashMap<String,String> headers;
        int writeTimeout;
        int readTimeout;
        int connectTimeout;
        Pair<SSLSocketFactory, X509TrustManager> sslFactory;
        HeaderCallable headerCallable;
        File cache;

        public Builder(Context context){
           this.context =  context.getApplicationContext();
        }
        public Builder baseUrl(String baseUrl){
            this.baseUrl = baseUrl;
            return this;
        }
        public Builder headers(HashMap<String,String> headers){
            this.headers = headers;
            return this;
        }
        public Builder cache(File cache){
            this.cache = cache;
            return this;
        }
        public Builder writeTimeout(int writeTimeout){
            this.writeTimeout = writeTimeout;
            return this;
        }
        public Builder readTimeout(int readTimeout){
            this.readTimeout = readTimeout;
            return this;
        }
        public Builder connectTimeout(int connectTimeout){
            this.connectTimeout = connectTimeout;
            return this;
        }
        public Builder headerCallable(HeaderCallable headerCallable){
            this.headerCallable = headerCallable;
            return this;
        }

        /**
         * 信任所有证书,不安全有风险
         *
         * @return
         */
        public Builder unsafeSSLSocketFactory(){
           this.sslFactory =  HttpsDelegate.getUnsafeSslSocketFactory();

            return this;
        }
        /**
         * 使用预埋证书，校验服务端证书（自签名证书）
         * @return
         */
        public Builder unsafeSSLSocketFactory(InputStream[] certificates){
           this.sslFactory =  HttpsDelegate.getUnsafeSslSocketFactory(certificates);
            return this;
        }

        /**
         *  使用bks证书和密码管理客户端证书（双向认证），使用预埋证书，校验服务端证书（自签名证书）
         * @param certificates
         * @param bksFile
         * @param password
         * @return
         */
        public Builder safeSSLSocketFactory(InputStream[] certificates, InputStream bksFile, String password){
           this.sslFactory =  HttpsDelegate.getSslSocketFactory(certificates,bksFile,password);
            return this;
        }
        public RetrofitClient build(){
            OkHttpClient.Builder builder = OkHttpClientBuilder.builder(context,cache);
            if(baseUrl ==null){
                throw new IllegalArgumentException("baseUrl can't be null");
            }
            if(connectTimeout>0){
                builder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
            }
            if(readTimeout>0){
                builder.readTimeout(readTimeout, TimeUnit.SECONDS);
            }
            if(writeTimeout>0){
                builder.writeTimeout(writeTimeout, TimeUnit.SECONDS);

            }
            if(sslFactory != null){
                builder.sslSocketFactory(sslFactory.first,sslFactory.second);
                builder.hostnameVerifier(new UnSafeHostnameVerifier());
            }
            HeaderInterceptor headerInterceptor= new HeaderInterceptor(headerCallable,baseUrl);
            if(headers !=null){
                headerInterceptor.putHeaders(headers);
            }
            builder.addInterceptor(headerInterceptor);
            Retrofit  retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(builder.build())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
            return new RetrofitClient(retrofit,headerInterceptor);
        }
    }
    public  <T> T create(Class<T> service){
        return ProxyHandler.create(retrofit,service);
    }

    public static RetrofitClient getDefault(){
        if(mDefaultRetrofitClient==null){
            mDefaultRetrofitClient = RetrofitClient.newBuilder(null).build();
        }
        return mDefaultRetrofitClient;
    }



}

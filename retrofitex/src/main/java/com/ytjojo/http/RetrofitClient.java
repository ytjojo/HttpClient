package com.ytjojo.http;

import android.support.v4.util.Pair;

import com.orhanobut.logger.PrettyFormatStrategy;
import com.ytjojo.http.coverter.GsonConverterFactory;
import com.ytjojo.http.https.HttpsDelegate;
import com.ytjojo.http.https.UnSafeHostnameVerifier;
import com.ytjojo.http.interceptor.HeaderCallable;
import com.ytjojo.http.interceptor.HeaderInterceptor;
import com.ytjojo.http.interceptor.HttpLoggingInterceptor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.MergeParameterHandler;
import retrofit2.ProxyHandler;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;

public class RetrofitClient {
    public static  final String ContentType_JSON = "application/json";
    public static  final String ContentType_FORM = "application/x-www-form-urlencoded; charset=UTF-8";
    private Retrofit retrofit ;
    OkHttpClient mOkHttpClient;
    public RetrofitClient(Retrofit retrofit){
        this.retrofit = retrofit;
    }
    public RetrofitClient(Retrofit retrofit,HeaderInterceptor headerInterceptor){
        this.mHeaderInterceptor = headerInterceptor;
        this.retrofit = retrofit;
    }
    private void setOkHttpClient(OkHttpClient okHttpClient){
        this.mOkHttpClient = okHttpClient;
    }
    public static RetrofitClient mDefaultRetrofitClient;
    private HeaderInterceptor mHeaderInterceptor;
    public void putHeader(String key,String value){
        mHeaderInterceptor.putHeader(key,value);
    }

    private static MergeParameterHandler sMergeParameterHandler;

    public static MergeParameterHandler getMergeParameterHandler(){
        return sMergeParameterHandler;
    }

    /**
     * 用户在登录页面登录成功之后
     * 获得token 要调用此方法
     * 方法会重置自动刷新token的标识
     * @param key
     * @param token
     */
    public void onUserLoginGetToken(String key,String token){
        mHeaderInterceptor.onUserLoginGetToken(key,token);
    }

    public void clearCached(){
        try {
            mOkHttpClient.cache().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void init(String baseUrl){
        mDefaultRetrofitClient = RetrofitClient.newBuilder().unsafeSSLSocketFactory().baseUrl(baseUrl).build();
    }
    public static void init(Builder builder){
        mDefaultRetrofitClient = builder.unsafeSSLSocketFactory().build();
    }
    public static void setDefault(RetrofitClient client){
        mDefaultRetrofitClient = client;
    }

    public static Builder newBuilder(){
        return new Builder();
    }
    public static class Builder{
        CookieJar cookieJar;
        String baseUrl;
        HashMap<String,String> headers;
        int writeTimeout;
        int readTimeout;
        int connectTimeout;
        Pair<SSLSocketFactory, X509TrustManager> sslFactory;
        HeaderCallable headerCallable;
        File cache;
        Converter.Factory factory;
        OkHttpClient okHttpClient;
        HttpLoggingInterceptor logging;
        ArrayList<Interceptor> interceptors = new ArrayList<>();
        public Builder(){

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
        public Builder cookie(CookieJar cookieJar){
            this.cookieJar =  cookieJar;
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
        public Builder addInterceptor(Interceptor interceptor){
            interceptors.add(interceptor);
            return this;
        }

        public Builder showLog(boolean showLog){
            if(!showLog){
                return this;
            }
            this.logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {

                PrettyFormatStrategy  pfs = PrettyFormatStrategy.newBuilder().methodCount(0).methodOffset(5).showThreadInfo(false).build();
                @Override
                public void log(String message) {
                    if (Platform.get() == Platform.Android) {

                        pfs.log(4,"http",message);

                    } else {
                        System.out.println("http =====  :  " + message);
                    }
                }
            });
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
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
        public Builder addConverterFactory(Converter.Factory factory){
           this.factory = factory;
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
        public Builder okhttpClient(OkHttpClient client){
            this.okHttpClient = client;
            return this;
        }
        public Builder mergeParameterHandler(MergeParameterHandler handler){
            RetrofitClient.sMergeParameterHandler = handler;
            return this;
        }
        public RetrofitClient build(){
            HeaderInterceptor headerInterceptor = null;
            if(baseUrl ==null){
                throw new IllegalArgumentException("baseUrl can't be null");
            }
            if(okHttpClient == null){

                OkHttpClient.Builder builder = OkHttpClientBuilder.builder(cookieJar,cache);

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
               headerInterceptor = new HeaderInterceptor(headerCallable,baseUrl);
                if(headers !=null){
                    headerInterceptor.putHeaders(headers);
                }
                builder.addInterceptor(headerInterceptor);
                if(logging !=null){
                    builder.addInterceptor(logging);
                }
                for(Interceptor i:interceptors){
                    builder.addInterceptor(i);
                }
                this.okHttpClient = builder.build();
            }
            
            this.factory = factory ==null?GsonConverterFactory.create():factory;
    
            Retrofit  retrofit = new Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(factory)
            .build();
            RetrofitClient retrofitClient = new RetrofitClient(retrofit,headerInterceptor);
            retrofitClient.setOkHttpClient(okHttpClient);
            return retrofitClient;
        }
    }
    public  <T> T create(Class<T> service){
        return ProxyHandler.create(retrofit,service);
    }

    public static RetrofitClient getDefault(){
        return mDefaultRetrofitClient;
    }



}

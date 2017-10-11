package com.ytjojo.http;

import com.ytjojo.http.cache.CacheInterceptor;
import com.ytjojo.http.interceptor.CacheControInterceptor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

public class OkHttpClientBuilder {


    private OkHttpClientBuilder() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    public static OkHttpClient.Builder builder(CookieJar cookieJar, File cacheDir) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);
        if(cookieJar !=null){
            builder .cookieJar(cookieJar);
        }
        File postCache =null;
        File getCache = null;
        if(cacheDir !=null){
            postCache = new File(cacheDir, "httpGet");
            getCache = new File(cacheDir, "httpPost");
            int maxCacheSize = 20 * 1024 * 1024;
            Cache cache = new Cache(getCache, maxCacheSize);
            builder.cache(cache);
            builder.addInterceptor(new CacheInterceptor(new com.ytjojo.http.cache.Cache(postCache, 20 * 1024 * 1024)));
            builder.addNetworkInterceptor(new CacheControInterceptor());
        }
        return builder;

    }


}

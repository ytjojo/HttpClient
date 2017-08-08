package com.ytjojo.http;

import android.content.Context;
import android.util.Log;

import com.ytjojo.http.cache.CacheInterceptor;
import com.ytjojo.http.interceptor.CacheControInterceptor;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientBuilder {


    private OkHttpClientBuilder() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    public static OkHttpClient.Builder builder(Context c,File cacheDir) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                if (Platform.get() == Platform.Android) {
                    Log.e("http", message);
                } else {
                    System.out.println("http =====  :  " + message);
                }
            }
        });
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(logging)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS);

        if (c != null) {
            int maxCacheSize = 15 * 1024 * 1024;
            File parent = cacheDir ==null ?c.getApplicationContext().getCacheDir():cacheDir;
            Cache cache = new Cache(new File(parent, "httpGet"), maxCacheSize);
            builder.addInterceptor(new CacheControInterceptor(c));
            builder.cache(cache)
                    .cookieJar(new CookiesManager(c))
                    .addInterceptor(new CacheInterceptor(new com.ytjojo.http.cache.Cache(new File(parent, "httpPost"), 20 * 1024 * 1024)));
        }
        return builder;

    }


}

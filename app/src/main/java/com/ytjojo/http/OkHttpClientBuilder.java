package com.ytjojo.http;

import android.content.Context;
import android.util.Log;
import com.ytjojo.http.cache.CacheInterceptor;
import java.util.concurrent.TimeUnit;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class OkHttpClientBuilder {


    private OkHttpClientBuilder() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    public static OkHttpClient.Builder builder(Context c) {
        int maxCacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(c.getApplicationContext().getCacheDir(), maxCacheSize);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                if(Platform.get()== Platform.PlatFormType.Android){
                    Log.e("http",message);
                }else{
                    System.out.println("http =====  :  "+message);
                }
            }
        });
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new CacheInterceptor(new com.ytjojo.http.cache.Cache(c.getExternalCacheDir(),20*1024*1024)))
                .cache(cache)
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20,TimeUnit.SECONDS)
                .cookieJar(new CookiesManager(c));

    }


}

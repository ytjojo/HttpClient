package com.ytjojo.http;

import android.content.Context;
import android.util.Log;
import com.ytjojo.http.cache.CacheInterceptor;
import com.ytjojo.videoHttp.LoggerInterceptor;
import java.io.IOException;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CustomerOkHttpClient {
    public void clearCached(){
        try {
            client.cache().delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static OkHttpClient client;

    private CustomerOkHttpClient() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }


    private static void create(Context c) {
        int maxCacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(c.getApplicationContext().getCacheDir(), maxCacheSize);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                Log.e("http",message);
            }
        });
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        // add your other interceptors …
        // add logging as last interceptor
        client = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("Request",true))
                .addInterceptor(logging)
                .addInterceptor(new CacheInterceptor(new com.ytjojo.http.cache.Cache(c.getExternalCacheDir(),20*1024*1024)))
                .addInterceptor(new Interceptor() {
                    @Override public Response intercept(Chain chain) throws IOException {
                        Request request= chain.request();
                        if(RetrofitClient.TOKEN !=null){
                           request = request.newBuilder().header(RetrofitClient.TOKEN_HEADER_KEY,RetrofitClient.TOKEN).build();
                        }
                        Response response = chain.proceed(request);
                        Response net = response.networkResponse();
                        Response cache = response.cacheResponse();
                        String cacheControl = request.cacheControl().toString();
                        return response;
                    }
                })
                .cache(cache)
//                .cookieJar(new CookiesManager(c))
                .build();
//        client.networkInterceptors().add(new StethoInterceptor());
    }

    public static OkHttpClient getInitClient(Context c) {
        if (client == null) {
            create(c);
        }
        return client;
    }
    public static OkHttpClient getClient(){
        return client;
    }

}

package com.ytjojo.http;

import android.content.Context;
import com.ytjojo.utils.TextUtils;
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


    static Interceptor cacheInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            String cacheControl = request.cacheControl().toString();
            if (TextUtils.isEmpty(cacheControl)) {
                cacheControl = "public, max-age=60 ,max-stale=2419200";
            }
            return response.newBuilder()
                    .header("Cache-Control", cacheControl)
                    .removeHeader("Pragma")
                    .build();
        }
    };

    private static void create(Context c) {
        int maxCacheSize = 10 * 1024 * 1024;
        Cache cache = new Cache(c.getApplicationContext().getCacheDir(), maxCacheSize);
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);


        // add your other interceptors …
        // add logging as last interceptor
        client = new OkHttpClient.Builder()
                .addNetworkInterceptor(cacheInterceptor)
                .addInterceptor(new LoggerInterceptor("Request",true))
//                .addInterceptor(logging)
//                .cache(cache)
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

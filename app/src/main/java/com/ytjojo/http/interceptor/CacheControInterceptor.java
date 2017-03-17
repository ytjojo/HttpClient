package com.ytjojo.http.interceptor;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/18 0018.
 */
public class CacheControInterceptor implements Interceptor {
    final int cacheTime;
    public CacheControInterceptor(int cacheTime){
        this.cacheTime = cacheTime;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response originalResponse = chain.proceed(chain.request());
        return originalResponse.newBuilder()
                .removeHeader("Pragma")
                .header("Cache-Control", String.format("max-age=%d", cacheTime))
                .build();
    }
}

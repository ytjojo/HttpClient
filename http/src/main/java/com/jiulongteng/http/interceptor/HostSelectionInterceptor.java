package com.jiulongteng.http.interceptor;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public final class HostSelectionInterceptor implements Interceptor {
    private volatile String host;

    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if (this.host != null) {
            HttpUrl httpUrl = request.url().newBuilder().host(host).build();
            request = request.newBuilder().url(httpUrl).build();
        }
        return chain.proceed(request);
    }

    public void setHost(String paramString) {
        this.host = paramString;
    }
}


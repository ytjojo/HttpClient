package com.ytjojo.http.download;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor for download
 * Created by JokAr on 16/5/11.
 */
public class DownloadProgressInterceptor implements Interceptor {

    private ProgressListener listener;


    public DownloadProgressInterceptor(ProgressListener listener) {
        this.listener = listener;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder().header("Content-Type", "multipart/form-data")
                .build();
        return chain.proceed(request);

    }
}
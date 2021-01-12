package com.jiulongteng.http.interceptor;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Invocation;

public final class InvocationLogger implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long startNanos = System.nanoTime();
        Response response = chain.proceed(request);
        long elapsedNanos = System.nanoTime() - startNanos;

        Invocation invocation = request.tag(Invocation.class);
        if (invocation != null) {
            System.out.printf(
                    "%s.%s %s HTTP %s (%.0f ms)%n",
                    invocation.method().getDeclaringClass().getSimpleName(),
                    invocation.method().getName(),
                    invocation.arguments(),
                    response.code(),
                    elapsedNanos / 1_000_000.0);
        }

        return response;
    }
}


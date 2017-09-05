package com.ytjojo.http.interceptor;

import com.ytjojo.http.exception.AuthException;
import com.ytjojo.http.exception.TokenInvalidException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//https://github.com/alighters/AndroidDemos/blob/master/app/src/main/java/com/lighters/demos/token/http/api/ErrorCode.java
public class HeaderInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final Map<String, String> mHeaders = new ConcurrentHashMap<>();
    private final HeaderCallable mTokenCallable;
    private final CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private final String baseUrl;
    AtomicInteger mRefreshTokenFlag = new AtomicInteger(0);
    volatile boolean restoreCachedValue;

    public void putHeader(String key, String value) {
        if (key != null && value != null)
            mHeaders.put(key, value);
    }

    public void putHeaders(HashMap<String, String> headers) {
        if (headers != null) {
            mHeaders.putAll(headers);
        }

    }

    public void onUserLoginGetToken(String key, String token) {
        mRefreshTokenFlag.set(0);
        if (key != null && token != null) {
            mHeaders.put(key, token);
        }
    }

    public HeaderInterceptor(HeaderCallable tokenCallable, String baseUrl) {
        this.mTokenCallable = tokenCallable;
        this.baseUrl = baseUrl;
    }

    void clearAuth() {
        mHeaders.clear();
    }

    void processAuth() throws AuthException {
        try {
            String value = mTokenCallable.call();
            if (value != null) {
                mHeaders.put(mTokenCallable.key(), value);
                HashMap<String, String> extraHeaders = mTokenCallable.extraHeaders();
                if (extraHeaders != null && !extraHeaders.isEmpty()) {
                    mHeaders.putAll(extraHeaders);
                }
            } else {
                throw new AuthException("HeaderCallable.call()获得的headervalue为null");
            }
        } catch (Exception e) {
            throw new AuthException(e);
        }

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();

        if (request.url().toString().startsWith(baseUrl)) {
            Response response = chain.proceed(updateHeadaerIfNeeded(chain));
            if (isTokenExpired(response)) {
                //token 已经失效了
                if (mTokenCallable != null) {
                    //判断是否已经刷新了header
                    String oldTokenValue = request.header(mTokenCallable.key());
                    if (mRefreshTokenFlag.compareAndSet(2, 0)) {
                        if (oldTokenValue != null && oldTokenValue.equals(mHeaders.get(mTokenCallable.key()))) {
                            //已经更新了header,但是服务器仍然验证失败
                            throw new TokenInvalidException(response.code(),response.message());
                        }
                    }
                    requestTokenAync();
                }

                final Request.Builder requestBuilder = request.newBuilder();
                for (HashMap.Entry<String, String> entry : mHeaders.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    if (value != null && key != null) {
                        requestBuilder.header(key, value);
                    }
                }
                Request newSigned = requestBuilder.build();
                response =  chain.proceed(newSigned);
                if(isTokenExpired(response)){
                    throw new TokenInvalidException(response.code(),response.message());
                }
            } else {
                return response;
            }
        }
        return chain.proceed(request);
    }

    public static boolean equals(Object o1, Object o2) {
        if (o1 == null || o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    private void requestTokenAync() throws IOException {
        if (mRefreshTokenFlag.compareAndSet(0, 1)) {
            clearAuth();
            try {
                processAuth();
                mRefreshTokenFlag.set(2);
            } catch (AuthException e) {
                mRefreshTokenFlag.set(3);
                throw e;
            } catch (Exception e) {
                mRefreshTokenFlag.set(3);
                throw new AuthException(e);
            } finally {
                mCountDownLatch.countDown();
            }
        } else if (mRefreshTokenFlag.compareAndSet(1, 1)) {
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public Request updateHeadaerIfNeeded(Chain chain) throws IOException {
        Request request = chain.request();
        if (mHeaders.isEmpty()) {
            return request;
        }
        final Request.Builder requestBuilder = request.newBuilder();
        if (mTokenCallable != null && !restoreCachedValue) {
            synchronized (HeaderInterceptor.class) {
                if (!restoreCachedValue) {
                    if (mHeaders.get(mTokenCallable.key()) == null) {
                        String cachedValue = mTokenCallable.getCachedValue();
                        if (cachedValue != null) {
                            mHeaders.put(mTokenCallable.key(), cachedValue);
                        }
                    }
                    restoreCachedValue = true;
                }
            }


        }
        for (HashMap.Entry<String, String> entry : mHeaders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (value != null && key != null) {
                requestBuilder.header(key, value);
            }
        }
        return requestBuilder.build();
    }

    private boolean isTokenExpired(Response originalResponse) {
        if (originalResponse.code() == 401) {
            return true;
        }
        if (mTokenCallable != null) {
            return mTokenCallable.isExpired(originalResponse.code(), originalResponse);
        }
//        ResponseBody responseBody = originalResponse.body();
//        BufferedSource source = responseBody.source();
//        source.request(Long.MAX_VALUE); // Buffer the entire body.
//        Buffer buffer = source.buffer();
//        Charset charset = UTF8;
//        MediaType contentType = responseBody.contentType();
//        if (contentType != null) {
//            charset = contentType.charset(UTF8);
//        }
//        String bodyValue = buffer.clone().readString(charset);
//
//        JSONObject jsonObject = new JSONObject(bodyValue);
//        int code = jsonObject.optInt("code");
//        if(code == ServerResponse.EXCEPTION_TOKEN_NOTVALID){
//            return true;
//        }
        return false;

    }

}
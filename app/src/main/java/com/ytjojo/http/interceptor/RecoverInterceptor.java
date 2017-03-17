package com.ytjojo.http.interceptor;

import com.ytjojo.utils.TextUtils;

import com.ytjojo.http.ResponseWrapper;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.exception.AuthException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
//https://github.com/alighters/AndroidDemos/blob/master/app/src/main/java/com/lighters/demos/token/http/api/ErrorCode.java
public class RecoverInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    String getAuth() {
        // check if we have auth, if not, authorize
        return RetrofitClient.TOKEN;
    }
    TokenCallable mTokenCallable;
    CountDownLatch mCountDownLatch = new CountDownLatch(1);
    public RecoverInterceptor(TokenCallable tokenCallable){
        this.mTokenCallable = tokenCallable;
    }
    void clearAuth() {
        RetrofitClient.TOKEN = null;

    }
    AtomicBoolean isTokenRequestRunning = new AtomicBoolean(false);
    void processAuth() throws Exception{
        RetrofitClient.TOKEN = mTokenCallable.call();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();

        if (request.url().toString().startsWith(RetrofitClient.BASE_URL)) {
            final Response response = chain.proceed(updateHeadaerIfNeeded(chain));
            try {
                if(isTokenExpired(response)){
                    requestTokenAync();
                    final Request newSigned = request.newBuilder()
                            .header(RetrofitClient.TOKEN_HEADER_KEY, getAuth())
                            .build();
                    return chain.proceed(newSigned);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return response;
        } else {
            return chain.proceed(request);
        }
    }
    private void requestTokenAync()throws IOException{
        if(isTokenRequestRunning.compareAndSet(false,true)){
            clearAuth();
            try {
                processAuth();

            } catch (IOException e){
                throw e;
            }
            catch (AuthException e){
                throw e;
            }
            catch (Exception e) {
                throw new AuthException(-2,"author unknow exception");
            }finally {
                mCountDownLatch.countDown();
            }
        }else{
            try {
                mCountDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    public Request updateHeadaerIfNeeded(Chain chain) throws IOException {
        Request origin = chain.request();
        Headers originHeaders = origin.headers();
        Headers.Builder newHeaders = new Headers.Builder();
        String authType = RetrofitClient.TOKEN;
        int size = originHeaders.size();
        for(int i=0;i < size;i++){
            if(TextUtils.equals(originHeaders.name(i),RetrofitClient.AUTHTYPE_HEADER_KEY)){
                newHeaders.add(originHeaders.name(i),originHeaders.value(i));
            }else{
                authType = originHeaders.value(i);
            }
        }
        if(TextUtils.isEmpty(authType)){
            authType = RetrofitClient.TOKEN;
        }
        Request.Builder newRequest = origin.newBuilder().headers(newHeaders.build());
        switch (authType){
            case RetrofitClient.AUTHTYPE_TOKEN:
                String token =originHeaders.get(RetrofitClient.TOKEN_HEADER_KEY);
                if(!TextUtils.isEmpty(RetrofitClient.TOKEN)&&(TextUtils.isEmpty(token)||!token.equals(RetrofitClient.TOKEN))){
                    newRequest.header(RetrofitClient.TOKEN_HEADER_KEY,RetrofitClient.TOKEN);
                }else{
                    requestTokenAync();
                    newRequest.header(RetrofitClient.TOKEN_HEADER_KEY,RetrofitClient.TOKEN);
                }
                break;
            case RetrofitClient.AUTHTYPE_BASIC:
            default:
                newRequest.removeHeader(RetrofitClient.TOKEN_HEADER_KEY);
                break;
        }
        return newRequest.build();

    }
    private boolean isTokenExpired(Response originalResponse) throws IOException, JSONException {
        ResponseBody responseBody = originalResponse.body();
        if(originalResponse.code() == 401){
            return true;
        }
        BufferedSource source = responseBody.source();
        source.request(Long.MAX_VALUE); // Buffer the entire body.
        Buffer buffer = source.buffer();
        Charset charset = UTF8;
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
            charset = contentType.charset(UTF8);
        }
        String bodyValue = buffer.clone().readString(charset);
        JSONObject jsonObject = new JSONObject(bodyValue);
        int code = jsonObject.optInt("code");
        if(code == ResponseWrapper.EXCEPTION_TOKEN_NOTVALID){
            return true;
        }
        return false;

    }

}
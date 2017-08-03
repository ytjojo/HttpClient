package com.ytjojo.http.interceptor;

import com.ytjojo.http.ResponseWrapper;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.exception.AuthException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import org.json.JSONException;
import org.json.JSONObject;
//https://github.com/alighters/AndroidDemos/blob/master/app/src/main/java/com/lighters/demos/token/http/api/ErrorCode.java
public class HeaderInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final HashMap<String,String> mHeaders =new HashMap<>();
    private final HeaderCallable mTokenCallable;
    private final CountDownLatch mCountDownLatch = new CountDownLatch(1);
    private final String baseUrl;
    public void putHeader(String key,String value){
        if(key !=null && value !=null)
        mHeaders.put(key,value);
    }
    public void putHeaders(HashMap<String,String> headers){
        if(headers!=null){

            mHeaders.putAll(headers);
        }

    }
    public HeaderInterceptor(HeaderCallable tokenCallable,String baseUrl){
        this.mTokenCallable = tokenCallable;
        this.baseUrl = baseUrl;
    }
    void clearAuth() {
       mHeaders.clear();

    }
    AtomicBoolean isTokenRequestRunning = new AtomicBoolean(false);
    void processAuth() throws AuthException{
        try{
            String value = mTokenCallable.call();
            if(value !=null){
                mHeaders.put(mTokenCallable.key(),value);
            }else{
                throw new AuthException("HeaderCallable.call()获得的headervalue为null");
            }
        }catch (Exception e){
            throw new AuthException(e);
        }

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        final Request request = chain.request();

        if (request.url().toString().startsWith(baseUrl)) {
            final Response response = chain.proceed(updateHeadaerIfNeeded(chain));
            try {
                if(isTokenExpired(response)){
                    if(mTokenCallable!=null){
                        requestTokenAync();
                    }
                    final Request.Builder requestBuilder = request.newBuilder();
                    for(HashMap.Entry<String,String> entry:mHeaders.entrySet()){
                        String key = entry.getKey();
                        String value = entry.getValue();
                        if(value !=null && key!=null ){
                            requestBuilder.header(key,value);
                        }
                    }
                    Request newSigned = requestBuilder.build();
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
            }
            catch (AuthException e){
                throw e;
            }
            catch (Exception e) {
                throw new AuthException(e);
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
        Request request = chain.request();
        if(mHeaders.isEmpty()){
            return request;
        }
        final Request.Builder requestBuilder = request.newBuilder();
        for(HashMap.Entry<String,String> entry:mHeaders.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            if(value !=null && key!=null ){
                requestBuilder.header(key,value);
            }
        }
        return requestBuilder.build();
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
package com.ytjojo.http.interceptor;

import java.util.concurrent.Callable;

/**
 * Created by Administrator on 2016/10/18 0018.
 */
public abstract class HeaderCallable implements Callable<String> {
//    HeaderInterceptor mHeaderInterceptor;
//
//    public void putHeader(String key,String value){
//        mHeaderInterceptor.putHeader(key,value);
//    }
    //public void request(){
    //    Response responseBody = OkHttpClientBuilder.getClient().newCall(getRequest()).execute();
    //    BufferedSource bufferedSource = Okio.buffer(responseBody.body().source());
    //    String value = bufferedSource.readUtf8();
    //    bufferedSource.close();
    //}
    public abstract String key();
}

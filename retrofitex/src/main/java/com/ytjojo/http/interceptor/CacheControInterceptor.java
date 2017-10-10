package com.ytjojo.http.interceptor;

import com.ytjojo.http.util.TextUtils;

import java.io.IOException;
import java.net.UnknownHostException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.ytjojo.http.cache.CacheInterceptor.HEADER_CACHE_TIME;

/**
 * Created by Administrator on 2016/12/18 0018.
 */
public class CacheControInterceptor implements Interceptor {
    public CacheControInterceptor(){
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if(!request.method().equals("GET")){
            return chain.proceed(request);
        }
        String cachetTime = request.header(HEADER_CACHE_TIME);
        request = request.newBuilder()
                .removeHeader(HEADER_CACHE_TIME)
                .build();
        Response originalResponse;
        try{
            originalResponse = chain.proceed(request);
        }catch (UnknownHostException e){
            request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
            return chain.proceed(request);
//            return originalResponse.newBuilder()
//                    .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
//                    .removeHeader("Pragma")
//                    .build();

        }
        //有网的时候读接口上的@Headers里的配置
        String cacheControl = request.cacheControl().toString();
        if(!TextUtils.isEmpty(cachetTime)){
            cacheControl = cachetTime;
        }
        return originalResponse.newBuilder()
                //.header("Cache-Control",  String.format("max-age=%d", cacheTime))
                .header("Cache-Control", cacheControl)
                .removeHeader("Pragma")
                .build();
    }


}

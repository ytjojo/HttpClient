package com.ytjojo.http.interceptor;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import java.io.IOException;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2016/12/18 0018.
 */
public class CacheControInterceptor implements Interceptor {
    final int cacheTime;
    Context context;
    public CacheControInterceptor(Context context,int cacheTime){
        this.cacheTime = cacheTime;
        this.context = context.getApplicationContext();
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //如果没有网络，则启用 FORCE_CACHE
        /**
         * 强制性使用本地缓存，如果本地缓存不满足条件，则会返回code为504
         */
        if (!isNetworkConnected()) {
            request = request.newBuilder()
                .cacheControl(CacheControl.FORCE_CACHE)
                .build();
        }
        Response originalResponse = chain.proceed(request);
        if (isNetworkConnected()) {
            //有网的时候读接口上的@Headers里的配置
            String cacheControl = request.cacheControl().toString();
            return originalResponse.newBuilder()
                //.header("Cache-Control",  String.format("max-age=%d", cacheTime))
                .header("Cache-Control", cacheControl)
                .removeHeader("Pragma")
                .build();
        } else {
            return originalResponse.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=2419200")
                .removeHeader("Pragma")
                .build();
        }
    }

    /**
     * 判断是否有网络
     *
     * @return 返回值
     */
    public boolean isNetworkConnected() {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}

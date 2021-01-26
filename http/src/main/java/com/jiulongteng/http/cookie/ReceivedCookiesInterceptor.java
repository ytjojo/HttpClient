package com.jiulongteng.http.cookie;

import android.util.Log;

import com.jiulongteng.http.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by allen on 2017/5/11.
 * <p>
 * 接受服务器发的cookie   并保存到本地
 */

public class ReceivedCookiesInterceptor implements Interceptor {
    volatile ConcurrentHashMap<String, List<String>> headers;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                for (String value : values) {
                    builder.header(key, value);
                    Log.v("OkHttp", "Adding Header: " + key + "  " + value); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
                }
            }
        }

        Response originalResponse = chain.proceed(builder.build());
        //这里获取请求返回的cookie
        List<String> accessList = originalResponse.headers("access-control-expose-headers");
        if (!CollectionUtils.isEmpty(accessList)) {
            for (String headerkey : accessList) {
                List<String> values = originalResponse.headers(headerkey);
                if (!CollectionUtils.isEmpty(values)) {
                    if (headers == null) {
                        headers = new ConcurrentHashMap<>();
                    }
                    headers.put(headerkey, values);
                }

            }
        }
        return originalResponse;
    }


}
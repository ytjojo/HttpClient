package com.ytjojo.http.interceptor;

import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static java.util.Calendar.getInstance;

/**
 * Created by allen on 2017/5/11.
 *
 * 接受服务器发的cookie   并保存到本地
 */

public class ReceivedCookiesInterceptor implements Interceptor {
    volatile ConcurrentLinkedQueue<String>  cookies;

    @Override public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        if(cookies !=null){
            for (String cookie : cookies) {
                builder.addHeader("Cookie", cookie);
                Log.v("OkHttp", "Adding Header: " + cookie); // This is done so I know which headers are being added; this interceptor is used after the normal logging of OkHttp
            }
        }

        Response originalResponse = chain.proceed(builder.build());
        //这里获取请求返回的cookie
        List<String> list = originalResponse.headers("Set-Cookie");
        if (!list.isEmpty()) {
            cookies = new ConcurrentLinkedQueue<>();
            for (String header : list) {
                cookies.add(header);
            }
        }
        //获取服务器相应时间--用于计算倒计时的时间差
//        String dateValue = originalResponse.header("Date");
//        if (!TextUtils.isEmpty(dateValue)){
//            long date = dateToStamp(originalResponse.header("Date"));
//        }
        return originalResponse;
    }

    /*
   * 将时间转换为时间戳
   */
    public static long dateToStamp(String s) throws android.net.ParseException {
//        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
//        Date date = new Date();
        Date date = new Date(s); //转换为标准时间对象
        Calendar calendar= getInstance();
        calendar.setTime(date);
        long mTimeInMillis = calendar.getTimeInMillis();
        return mTimeInMillis;
    }
}
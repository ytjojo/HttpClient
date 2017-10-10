package com.ytjojo.http.okhttpBuilder;

import android.support.annotation.StringDef;

import com.ytjojo.http.cache.CacheInterceptor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * Created by Administrator on 2017/10/8 0008.
 */

public interface CacheHeaderTime {
//    String NO_CACHE = "Cache-Control: no-cache";
//    String FORCE_CACHE = "Cache-Control: max-stale=2147483647, only-if-cached";
//    String CACHE_30_SEC = "Cache-Control: public, max-age=30";
//    String CACHE_60_SEC = "Cache-Control: public, max-age=60";
//    String CACHE_3_MIN = "Cache-Control: public, max-age=180";
//    String CACHE_5_MIN = "Cache-Control: public, max-age=300";
//    String CACHE_10_MIN = "Cache-Control: public, max-age=600";
//    String CACHE_30_MIN = "Cache-Control: public, max-age=1800";
//    String CACHE_1_HOR = "Cache-Control: public, max-age=3600";
//    String CACHE_24_HOR = "Cache-Control: public, max-age=86400";
//    String CACHE_1_WEEK = "Cache-Control: public, max-age=604800";
//    String CACHE_30_DAYS = "Cache-Control: public, max-age=2592000";
//    String CACHE_90_DAYS = "Cache-Control: public, max-age=7776000";
    String NO_CACHE = CacheInterceptor.HEADER_CACHE_TIME+": no-cache";
    String FORCE_CACHE =CacheInterceptor.HEADER_CACHE_TIME+": public, max-stale=2147483647, only-if-cached";
    String CACHE_30_SEC = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=30";
    String CACHE_60_SEC = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=60";
    String CACHE_3_MIN = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=180";
    String CACHE_5_MIN = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=300";
    String CACHE_10_MIN = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=600";
    String CACHE_30_MIN = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=1800";
    String CACHE_1_HOR = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=3600";
    String CACHE_24_HOR = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=86400";
    String CACHE_1_WEEK = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=604800";
    String CACHE_30_DAYS = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=2592000";
    String CACHE_90_DAYS = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=7776000";
    String CACHE_180_DAYS = CacheInterceptor.HEADER_CACHE_TIME+": public, max-age=15552000";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CacheHeaderTime.NO_CACHE,
            CacheHeaderTime.FORCE_CACHE,
            CacheHeaderTime.CACHE_30_SEC,
            CacheHeaderTime.CACHE_60_SEC,
            CacheHeaderTime.CACHE_3_MIN,
            CacheHeaderTime.CACHE_5_MIN,
            CacheHeaderTime.CACHE_10_MIN,
            CacheHeaderTime.CACHE_30_MIN,
            CacheHeaderTime.CACHE_1_HOR,
            CacheHeaderTime.CACHE_24_HOR,
            CacheHeaderTime.CACHE_1_WEEK,
            CacheHeaderTime.CACHE_30_DAYS,
            CacheHeaderTime.CACHE_90_DAYS,
            CacheHeaderTime.CACHE_180_DAYS
    })
    public @interface $CacheHeaderTime {
    }
}

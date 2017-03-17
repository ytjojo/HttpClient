package com.ytjojo.http.okhttpBuilder;

import android.content.Context;
import android.os.Build;
import android.os.StatFs;

import java.io.File;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2016/12/13 0013.
 */
public class ClientBuilder {

    /**
     * Set cache Dir
     */
    public static void cache(Context context, OkHttpClient.Builder builder, String dirName) {
        File cache = new File(context.getApplicationContext().getCacheDir(), dirName);
        if (!cache.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cache.mkdirs();
        }
        long size = 15 * 1024 * 1024;
        try {
            StatFs statFs = new StatFs(cache.getAbsolutePath());
            long count, blockSize;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1){
                count = statFs.getBlockCountLong();
                blockSize = statFs.getBlockSizeLong();
            } else {
                count = statFs.getBlockCount();
                blockSize = statFs.getBlockSize();
            }
            long available = count * blockSize;
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }
        // Bound inside min/max size for disk cache.
        size = Math.max(Math.min(size, size * 10), size);

        builder.cache(new Cache(cache, size)).build();
    }
    /**
     * Cancel all request.
     */
    public void cancelAll(OkHttpClient client){
        client.dispatcher().cancelAll();
    }

    /**
     * Cancel request with {@code tag}
     */
    public void cancel(OkHttpClient client,Object tag){
        for (Call call : client.dispatcher().queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : client.dispatcher().runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }



}

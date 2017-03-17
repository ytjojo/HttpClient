package com.ytjojo.common.glide;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskLruCacheWrapper;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.target.ViewTarget;
import com.ytjojo.practice.R;

import java.io.File;
import java.io.InputStream;

public class CustomGlideModule implements GlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        ViewTarget.setTagId(R.id.glide_tag_id); // 设置别的get/set tag id，以免占用View默认的
        builder.setDecodeFormat(DecodeFormat.PREFER_ARGB_8888); // 设置图片质量为高质量
        builder.setDiskCache(new DiskCache.Factory() {
            @Override
            public DiskCache build() {
                // 自己的缓存目录
                File imgFile = new File(Environment.getExternalStorageDirectory()+"/Android/body/"+context.getApplicationInfo().packageName);
                return DiskLruCacheWrapper.get(imgFile,DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
            }
        });
//        MemorySizeCalculator calculator = new MemorySizeCalculator(context);
//        builder.setMemoryCache(new LruResourceCache(calculator.getMemoryCacheSize()));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        // 注册我们的ImageFidLoader
        glide.register(RequestImageUrl.class, InputStream.class, new ImageUrlModelLoader.Factory());
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(15, TimeUnit.SECONDS)
//                .readTimeout(15, TimeUnit.SECONDS)
//                .build();
//
//        glide.register(GlideUrl.class, InputStream.class,
//                new OkHttpUrlLoader.Factory(client));
//        glide.setMemoryCategory(MemoryCategory.LOW);

    }
}
package com.ytjojo.common.glide;

import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelCache;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;

import java.io.InputStream;

public class ImageUrlModelLoader implements ModelLoader<RequestImageUrl,InputStream> {

    private final ModelCache<RequestImageUrl, RequestImageUrl> mModelCache;
    private InputStreamTransfer mInputStreamTransfer;
    public ImageUrlModelLoader() {
        this(null);
    }

    public ImageUrlModelLoader(ModelCache<RequestImageUrl, RequestImageUrl> modelCache) {
        mModelCache = modelCache;

    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(RequestImageUrl model, int width, int height) {
        RequestImageUrl requestImageUrl = model;
        // 从缓存中取出RequestImageUrl,ImgeFid已重写equals()和hashCode()方法
        // 缓存中ImgeFid对象的url，有可能还没被初始化
        if (mModelCache != null) {
            requestImageUrl = mModelCache.get(model, 0, 0);
            if (requestImageUrl == null) {
                mModelCache.put(model, 0, 0, model);
                requestImageUrl = model;
            }
        }
        return new ImageStreamFetcher(requestImageUrl,mInputStreamTransfer);
    }


    // ModelLoader工厂，在向Glide注册自定义ModelLoader时使用到
    public static class Factory implements ModelLoaderFactory<RequestImageUrl, InputStream> {
        // 缓存
        private final ModelCache<RequestImageUrl, RequestImageUrl> mModelCache = new ModelCache<>(500);
        
        @Override
        public ModelLoader<RequestImageUrl, InputStream> build(Context context,
            GenericLoaderFactory factories) {
            // 返回RequestImageUrlLoader对象
            return new ImageUrlModelLoader(mModelCache);
        }

        @Override
        public void teardown() {

        }
    }
    
}
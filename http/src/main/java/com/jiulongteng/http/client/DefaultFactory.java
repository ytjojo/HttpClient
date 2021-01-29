package com.jiulongteng.http.client;


import java.io.File;

public class DefaultFactory extends AbstractHttpClientFactory {
    static volatile DefaultFactory sInstance;


    public static DefaultFactory getInstance() {
        if (sInstance == null) {
            synchronized (DefaultFactory.class) {
                if (sInstance == null) {
                    sInstance = new DefaultFactory();
                }
            }
        }
        return sInstance;
    }


    @Override
    public File getHttpCacheParent() {
        return getContext().getCacheDir();
    }
}
  

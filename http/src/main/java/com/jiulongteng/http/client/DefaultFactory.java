package com.jiulongteng.http.client;



public class DefaultFactory extends AbstractHttpClientFactory {
    static DefaultFactory sInstance;

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

    public AbstractClient createByTag(Object tag) {
        return super.createByTag(tag);
    }

    public AbstractClient createByUrl(String baseUrl) {
        return super.createByUrl(baseUrl);
    }



    public AbstractClient getDefaultClient(String baseUrl) {
        return getByUrl(baseUrl);
    }
}
  

package com.jiulongteng.http.client;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jiulongteng.http.util.ContextProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public abstract class AbstractHttpClientFactory implements IHttpClientFactory {

    private OkHttpClient baseOkHttpClient;

    private boolean isShowLog = true;
    private ConcurrentHashMap<Object, AbstractClient> clientsByTag = new ConcurrentHashMap<Object, AbstractClient>();

    private ConcurrentHashMap<String, AbstractClient> clientsByUrl = new ConcurrentHashMap<String, AbstractClient>();

    private static Context sContext;

    private AbstractClient defaultClient;

    private IHttpClientBuilder httpClientBuilder;

    public AbstractClient createByTag(Object tag) {
        if (httpClientBuilder != null) {
            return httpClientBuilder.createByTag(tag);
        }
        return null;
    }

    public static void setApplication(Context context) {
        sContext = context.getApplicationContext();
    }

    public Context getContext() {
        if (sContext == null) {
            sContext = ContextProvider.getContext();
        }
        return sContext;
    }

    public AbstractClient createByUrl(String baseUrl) {
        if (httpClientBuilder != null) {
            AbstractClient client = httpClientBuilder.createByUrl(baseUrl);
            if (client != null) {
                return client;
            }
        }
        return new HttpClient(baseUrl);
    }

    @Override
    public OkHttpClient getBaseOkHttpClient() {
        if (this.baseOkHttpClient == null) {
            OkHttpClient.Builder builder = (new OkHttpClient()).newBuilder()
                    .connectTimeout(15L, TimeUnit.SECONDS).writeTimeout(15L, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .readTimeout(15L, TimeUnit.SECONDS);

            this.baseOkHttpClient = builder.build();
        }
        return this.baseOkHttpClient;
    }


    @Override
    public boolean isShowLog() {
        return isShowLog;
    }


    public AbstractClient getByTag(Object tag) {
        if (this.clientsByTag.get(tag) == null) {
            AbstractClient abstractClient = createByTag(tag);
            abstractClient.setTag(tag);
            putHttpClient(abstractClient);
        }
        return clientsByTag.get(tag);
    }

    public AbstractClient getByUrl(String baseUrl) {
        AbstractClient abstractClient = this.clientsByUrl.get(baseUrl);
        if (abstractClient == null) {
            abstractClient = createByUrl(baseUrl);
            putHttpClient(abstractClient);
        }
        return abstractClient;
    }

    public void putHttpClient(AbstractClient abstractClient) {
        if (abstractClient.getTag() != null) {
            this.clientsByTag.put(abstractClient.getTag(), abstractClient);
        } else {
            this.clientsByUrl.put(abstractClient.getBaseUrl(), abstractClient);
        }
        abstractClient.attachToFactory(this);
    }



    public void setHttpClientBuilder(IHttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

    public void setShowLog(boolean isShowLog) {
        this.isShowLog = isShowLog;
    }

    @Override
    @NonNull
    public synchronized AbstractClient getDefaultClient() {
        if(defaultClient != null){
            return defaultClient;
        }
        if (httpClientBuilder != null) {
           defaultClient = httpClientBuilder.getDefaultClient();
           if(defaultClient != null){
               defaultClient.attachToFactory(this);
           }
           return defaultClient;
        }
        return null;
    }
}


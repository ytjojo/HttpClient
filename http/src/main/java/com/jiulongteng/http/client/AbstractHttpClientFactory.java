package com.jiulongteng.http.client;

import android.content.Context;

import com.jiulongteng.http.util.ContextProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public abstract class AbstractHttpClientFactory implements IHttpClientFactory {
    OkHttpClient baseOkHttpClient;

    private boolean isShowLog;
    ConcurrentHashMap<Object, AbstractClient> clientsByTag = new ConcurrentHashMap<Object, AbstractClient>();

    ConcurrentHashMap<String, AbstractClient> clientsByUrl = new ConcurrentHashMap<String, AbstractClient>();

    private static Context sContext;

    public AbstractClient createByTag(Object tag) {
        if (httpClientBuilder != null) {
            return httpClientBuilder.createByTag(tag);
        }
        return null;
    }

    public static void setApplication(Context context) {
        sContext = context.getApplicationContext();
    }

    @Override
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

    private IHttpClientBuilder httpClientBuilder;

    public void setHttpClientBuilder(IHttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

    public void setShowLog(boolean isShowLog) {
        this.isShowLog = isShowLog;
    }

    @Override
    public AbstractClient getDefaultClient() {
        if (httpClientBuilder != null) {
            httpClientBuilder.getDefaultClient();
        }
        return null;
    }
}


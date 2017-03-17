package com.ytjojo.http.interceptor;

import com.ytjojo.http.CustomerOkHttpClient;
import com.ytjojo.http.exception.AuthException;

import java.util.concurrent.Callable;

import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by Administrator on 2016/10/18 0018.
 */
public abstract class TokenCallable implements Callable<String> {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String call() throws Exception {
        Response responseBody = CustomerOkHttpClient.getClient().newCall(getRequest()).execute();
        BufferedSource bufferedSource = Okio.buffer(responseBody.body().source());
        String value = bufferedSource.readUtf8();
        bufferedSource.close();
        return convertResponseToToken(value);
    }

    public abstract String convertResponseToToken(String value) throws AuthException;

    public abstract Request getRequest();
}

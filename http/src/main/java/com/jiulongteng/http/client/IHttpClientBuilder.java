package com.jiulongteng.http.client;

public interface IHttpClientBuilder {

    AbstractClient createByTag(Object tag);

    AbstractClient createByUrl(String baseUrl);

    AbstractClient getDefaultClient();
}

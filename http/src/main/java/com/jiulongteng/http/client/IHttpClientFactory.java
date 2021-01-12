package com.jiulongteng.http.client;

import okhttp3.OkHttpClient;

public interface IHttpClientFactory {
  AbstractClient createByTag(Object tag);
  
  AbstractClient createByUrl(String baseUrl);
  
  OkHttpClient getBaseOkHttpClient();
  
  AbstractClient getByTag(Object tag);
  
  AbstractClient getByUrl(String baseUrl);
  
  void putHttpClient(AbstractClient client);

  boolean isShowLog();

}

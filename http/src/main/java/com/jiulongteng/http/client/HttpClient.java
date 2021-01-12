package com.jiulongteng.http.client;

import java.io.File;

public class HttpClient extends AbstractClient {
  public HttpClient(String baeUrl) {
    super(baeUrl);
  }

  @Override
  public File getHttpCache() {
    return null;
  }

  @Override
  public long getMaxCacheSize() {
    return 50 * 1000_000;
  }
}



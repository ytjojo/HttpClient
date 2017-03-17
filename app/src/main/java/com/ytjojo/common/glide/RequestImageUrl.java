package com.ytjojo.common.glide;

/**
 * Created by Administrator on 2016/4/8 0008.
 */
public interface RequestImageUrl {
    String getId();
    String getUrl();
    String syncUrlAcquire();
    void setUrl(String url);
    void cancel();
}

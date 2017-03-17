package com.ytjojo.videoHttp;

/**
 * Created by Administrator on 2016/12/4 0004.
 */
public interface Callback<T> {
    void onSuccsess(T result);

    void onFail(int code, String msg);
}

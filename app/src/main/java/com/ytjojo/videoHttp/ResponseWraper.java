package com.ytjojo.videoHttp;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/4 0004.
 */
public class ResponseWraper<T> implements Serializable{
    public T body;
    public int code;
    public String msg;
}

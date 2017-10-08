package com.ytjojo.http.exception;

/**
 * Created by Administrator on 2017/10/8 0008.
 */

public class UnknownException extends RuntimeException {
    public String reponse;
    public UnknownException(String msg) {
        super(msg);
    }
    public UnknownException(String msg,Throwable throwable) {
        super(msg,throwable);
    }
    public UnknownException(Throwable throwable) {
        super(throwable);
    }
}

package com.ytjojo.http.exception;

/**
 * Created by Administrator on 2016/10/18 0018.
 */
public class AuthException extends RuntimeException {
    public int code;
    public String reponse;
    public AuthException(int code, String msg,String reponse) {
        super(msg);
        this.reponse = reponse;
    }
    public AuthException(int code, String msg) {
        super(msg);
    }
    public AuthException(String msg,Throwable throwable) {
        super(msg,throwable);
    }
    public int getCode(){
        return code;
    }
}

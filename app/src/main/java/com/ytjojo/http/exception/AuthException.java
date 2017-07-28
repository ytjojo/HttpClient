package com.ytjojo.http.exception;

/**
 * Created by Administrator on 2016/10/18 0018.
 */
public class AuthException extends RuntimeException {
    public int code;
    public String reponse;
    public AuthException(int code, String msg) {
        super(msg);
        this.code = code;
    }
    public AuthException(int code,String msg,Throwable throwable) {
        super(msg,throwable);
        this.code = code;
    }
    public int getCode(){
        return code;
    }
    public AuthException(String msg,Throwable throwable){
        super(msg,throwable);
    }
    public AuthException(Throwable throwable){
        super(throwable);
    }
}

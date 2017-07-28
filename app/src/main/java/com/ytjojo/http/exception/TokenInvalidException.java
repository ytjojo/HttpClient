package com.ytjojo.http.exception;

/**
 * Created by Administrator on 2016/10/20 0020.
 */
public class TokenInvalidException extends RuntimeException {
    public int code;
    public String reponse;
    public TokenInvalidException(int code, String msg,Throwable throwable) {
        super(msg,throwable);
        this.code = code;
    }
    public TokenInvalidException(String msg) {
        super(msg);
    }
    public TokenInvalidException( String msg,Throwable throwable) {
        super(msg,throwable);
    }
    public TokenInvalidException(int code, String msg) {
        super(msg);
    }
    public int getCode(){
        return code;
    }
}

package com.ytjojo.http.exception;

/**
 * Created by Administrator on 2016/10/20 0020.
 */
public class TokenInvalidException extends RuntimeException {
    public int code;
    public String reponse;
    public TokenInvalidException(int code, String msg,String reponse) {
        super(msg);
        this.reponse = reponse;
    }
    public TokenInvalidException(int code, String msg) {
        super(msg);
    }
    public int getCode(){
        return code;
    }
}

package com.jiulongteng.http.exception;

public class TokenInvalidException extends RuntimeException {
    public int code;
    public String response;

    public TokenInvalidException(int code, String msg, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
    }

    public TokenInvalidException(String msg) {
        super(msg);
    }

    public TokenInvalidException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public TokenInvalidException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}


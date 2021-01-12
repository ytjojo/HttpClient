package com.jiulongteng.http.exception;

public class AuthException extends RuntimeException {
  public int code;
  public String reponse;
  public AuthException(int code, String msg) {
    super(msg);
    this.code = code;
  }
  public AuthException(String msg) {
    super(msg);
  }
  public AuthException(int code,String msg,Throwable throwable) {
    super(msg,throwable);
    this.code = code;
  }
  public AuthException(int code,Throwable throwable) {
    super(throwable);
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


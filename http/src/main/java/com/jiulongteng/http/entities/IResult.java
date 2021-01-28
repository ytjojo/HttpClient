package com.jiulongteng.http.entities;


import okhttp3.Headers;

public interface IResult<T> {
    int getCode();

    String getMessage();

    boolean isSuccessful();

    boolean isInvalidToken();

    void setHeaders(Headers headers);

    Headers getHeaders();

    T getData();

}

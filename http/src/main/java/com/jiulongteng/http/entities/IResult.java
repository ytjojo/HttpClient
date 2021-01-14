package com.jiulongteng.http.entities;

public interface IResult {
    int getCode();
    String getMessage();

    boolean isSuccessful();
    boolean isInvalidToken();
}

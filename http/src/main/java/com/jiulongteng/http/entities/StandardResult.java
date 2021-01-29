package com.jiulongteng.http.entities;

import com.google.gson.annotations.SerializedName;
import com.jiulongteng.http.exception.APIException;

import okhttp3.Headers;

public class StandardResult<T> implements IResult<T> {
    @SerializedName(alternate = {"status"}, value = "code")
    public int code = 0;

    @SerializedName(alternate = {"body", "result"}, value = "data")
    public T data;

    private transient Headers headers;

    @SerializedName(alternate = {"msg"}, value = "message")
    private String message;

    private transient Throwable throwable;

    public StandardResult() {
    }

    public StandardResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public StandardResult(int code, String message, Headers headers) {
        this.code = code;
        this.message = message;
        this.headers = headers;
    }

    public StandardResult(Throwable throwable) {
        if (throwable.getCause() instanceof APIException) {
            APIException aPIException = (APIException) throwable.getCause();
            this.code = aPIException.code;
            this.message = aPIException.getMessage();
        }
        this.throwable = throwable;
    }

    @Override
    public int getCode() {
        return code;
    }

    public String getMessage() {
        Throwable throwable = this.throwable;
        return (throwable != null) ? throwable.getMessage() : this.message;
    }



    @Override
    public boolean isInvalidToken() {
        return getCode() == 40400;
    }

    @Override
    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    @Override
    public Headers getHeaders() {
        return headers;
    }

    @Override
    public boolean isSuccessful() {
        return (code == 0 || code == 200);
    }

    @Override
    public T getData() {
        return data;
    }
}



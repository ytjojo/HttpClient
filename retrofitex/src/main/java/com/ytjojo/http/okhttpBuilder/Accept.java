package com.ytjojo.http.okhttpBuilder;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface Accept {
    String EMPTY = "";
    String ACCEPT_JSON = "application/json;charset=utf-8";
    String ACCEPT_TEXT = "text/html;charset=utf-8";
    String ACCEPT_DATA = "application/octet-stream";
    String ACCEPT_IMAGE = "image/png,image/jpeg,image/*";
    String ACCEPT_FILE = "application/octet-stream";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            Accept.EMPTY,
            Accept.ACCEPT_JSON,
            Accept.ACCEPT_TEXT,
            Accept.ACCEPT_DATA,
            Accept.ACCEPT_IMAGE,
            Accept.ACCEPT_FILE
    })
    public @interface $Accept {
    }
}
package com.ytjojo.http.exception;

import java.io.IOException;

/**
 * Created by Administrator on 2017/11/12 0012.
 */

public class JsonException extends IOException {

    public JsonException() {
        super();
    }
    public JsonException(String message) {
        super(message);
    }

    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonException(Throwable cause) {
        super(cause);
    }
}

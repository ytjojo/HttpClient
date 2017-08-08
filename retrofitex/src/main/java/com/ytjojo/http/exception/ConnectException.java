package com.ytjojo.http.exception;

import java.io.IOException;

/**
 * Created by Administrator on 2017/8/7 0007.
 */

public class ConnectException extends IOException {

    /**
     * Constructs a new {@code IOException} with its stack trace filled in.
     */
    public ConnectException() {
    }

    /**
     * Constructs a new {@code IOException} with its stack trace and detail
     * message filled in.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public ConnectException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new instance of this class with detail message and cause
     * filled in.
     *
     * @param message
     *            The detail message for the exception.
     * @param cause
     *            The detail cause for the exception.
     * @since 1.6
     */
    public ConnectException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new instance of this class with its detail cause filled in.
     *
     * @param cause
     *            The detail cause for the exception.
     * @since 1.6
     */
    public ConnectException(Throwable cause) {
        super(cause == null ? null : cause.toString(), cause);
    }
}

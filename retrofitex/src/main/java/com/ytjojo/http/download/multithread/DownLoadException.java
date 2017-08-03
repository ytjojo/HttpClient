package com.ytjojo.http.download.multithread;

/**
 * Created by Administrator on 2016/11/12 0012.
 */
public class DownLoadException extends RuntimeException {
    public int code;
    /**
     * Constructs a new {@code RuntimeException} that includes the current stack
     * trace.
     */
    public DownLoadException() {
    }
    public DownLoadException(int code,String msg) {
        super(msg);
        this.code = code;
    }
    public DownLoadException(int code,String msg,Throwable e) {
        super(msg,e);
        this.code = code;
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified detail message.
     *
     * @param detailMessage
     *            the detail message for this exception.
     */
    public DownLoadException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace,
     * the specified detail message and the specified cause.
     *
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public DownLoadException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    /**
     * Constructs a new {@code RuntimeException} with the current stack trace
     * and the specified cause.
     *
     * @param throwable
     *            the cause of this exception.
     */
    public DownLoadException(Throwable throwable) {
        super(throwable);
    }
}

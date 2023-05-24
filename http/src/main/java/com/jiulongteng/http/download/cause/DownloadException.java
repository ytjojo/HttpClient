package com.jiulongteng.http.download.cause;

public class DownloadException extends RuntimeException {
    public static final int SERVER_ERROR = 0;
    public static final int PROTOCOL_ERROR = 1;
    public static final int SERVER_CANCEL_ERROR = 2;
    public static final int DOWNLOAD_SECURITY_ERROR = 3;
    public static final int INTERRUPT_ERROR = 4;
    public static final int NETWORK_POLICY_ERROR = 5;
    public static final int FILENAME_NOT_FOUND_ERROR = 6;
    public static final int RESUME_ERROR = 7;
    public static final int PREALLOCATE_ERROR = 8;



    private int code;

    public DownloadException(int code, String message) {
        super(message);
        this.code = code;
    }

    public DownloadException(String message) {
        super(message);
    }

}

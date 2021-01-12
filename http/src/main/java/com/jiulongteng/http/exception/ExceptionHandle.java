package com.jiulongteng.http.exception;

import android.util.Log;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import retrofit2.HttpException;

/**
 * ExceptionHandle
 */
public class ExceptionHandle {

    private static final int UNAUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int REQUEST_TIMEOUT = 408;
    private static final int REQUEST_CONFLICT = 409;
    private static final int INTERNAL_SERVER_ERROR = 500;
    private static final int BAD_GATEWAY = 502;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int GATEWAY_TIMEOUT = 504;


    /**
     * @param e
     * @return
     */
    public static ResponeThrowable handleException(Throwable e) {
        ResponeThrowable ex;
        Log.i("rxjava-http-error", "e toStr==>" + e.toString());
        if (e instanceof HttpException) {
            HttpException httpException = (HttpException) e;

            switch (httpException.code()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                case REQUEST_CONFLICT:
                    ex = new ResponeThrowable("登录信息已经过期，请重新登录", e, ERROR.TOKEN_ERROR);
                    break;
                case NOT_FOUND:
                case REQUEST_TIMEOUT:
                case GATEWAY_TIMEOUT:
                case INTERNAL_SERVER_ERROR:
                case BAD_GATEWAY:
                case SERVICE_UNAVAILABLE:
                default:
                    ex = new ResponeThrowable("您的网络开小差啦", e, ERROR.HTTP_ERROR);
                    //ex.code = httpException.code();
                    break;
            }
            return ex;
        } else if (e instanceof APIException) {
            ex = new ResponeThrowable(e.getMessage(), e, ((APIException) e).code);
            return ex;
        } else if (e instanceof ConnectException ||
                e instanceof SocketTimeoutException ||
                e instanceof TimeoutException ||
                e instanceof UnknownHostException ||
                e instanceof EOFException) {
            ex = new ResponeThrowable("无法连接到网络，请确认网络连接",
                    e, ERROR.NETWORK_ERROR);
            return ex;
        } else if (e instanceof javax.net.ssl.SSLHandshakeException) {
            ex = new ResponeThrowable("证书验证失败", e, ERROR.SSL_ERROR);
            return ex;

        } else if (e instanceof TokenInvalidException) {
            // ex = new ResponeThrowable("登录信息已经过期，请重新登陆...", e.getCause(), ERROR.TOKEN_ERROR);
            // 40400 不显示toast
            ex = new ResponeThrowable("", e.getCause(), ERROR.TOKEN_ERROR);
            return ex;

        } else if (e instanceof AuthException) {
            ex = new ResponeThrowable("登录信息已经过期，请重新登录", e.getCause(), ERROR.AUTH_ERROR);
            return ex;
        } else {
            String className = e.getClass().getName().toLowerCase();
            if (className.contains("json")) {
                ex = new ResponeThrowable("不好意思，解析数据出错", e, ERROR.PARSE_ERROR);
                return ex;
            }

            ex = new ResponeThrowable("无法连接到服务器", e.getCause() == null
                    ? e : e.getCause(), ERROR.UNKNOWN);
            return ex;
        }
    }


    /**
     * 约定异常
     */
    public static final class ERROR {
        /**
         * 未知错误
         */
        public static final int UNKNOWN = 1000;
        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 1001;
        /**
         * 网络错误
         */
        public static final int NETWORK_ERROR = 1002;

        /**
         * 提交数据时候,出现断网或网络不给力,
         * 需额外使用单独的文案
         */
        public static final int NETWORK_ERROR_IN_SEND_DATA = 100201;

        /**
         * 协议出错
         */
        public static final int HTTP_ERROR = 1003;

        /**
         * 证书出错
         */
        public static final int SSL_ERROR = 1005;
        /**
         * TOKEN失效
         */
        public static final int TOKEN_ERROR = 1006;
        /**
         * 登陆出错
         */
        public static final int AUTH_ERROR = 1007;
    }

    /**
     * ResponeThrowable
     */
    public static class ResponeThrowable extends RuntimeException {
        public int code;

        public ResponeThrowable(String message, Throwable throwable, int code) {
            super(message, throwable);
            this.code = code;
        }
    }


}
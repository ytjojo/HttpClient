package com.ytjojo.http.exception;

/**
 * Created by Administrator on 2017/8/4 0004.
 */

public interface HttExceptionHandlar {
	public Throwable onError(int code,String msg);
}

package com.jiulongteng.http.util;

public class LogUtil {

    public static void logThread(String prefix) {

        System.out.println(prefix + "  id =  " + Thread.currentThread().getId() + "   name =" + Thread.currentThread().getName() + " " + Thread.currentThread().toString());
    }
}

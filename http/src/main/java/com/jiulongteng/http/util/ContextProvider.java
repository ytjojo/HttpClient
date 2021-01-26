package com.jiulongteng.http.util;

import android.content.Context;

import java.lang.reflect.Method;

public final class ContextProvider {

    /**
     * Context对象
     */
    private static Context CONTEXT_INSTANCE;

    /**
     * 取得Context对象
     * PS:必须在主线程调用
     *
     * @return Context
     */
    public static Context getContext() {
        if (CONTEXT_INSTANCE == null) {
            synchronized (ContextProvider.class) {
                if (CONTEXT_INSTANCE == null) {
                    try {
                        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");

                        Method method = ActivityThread.getMethod("currentActivityThread");
                        method.setAccessible(true);
                        Object currentActivityThread = method.invoke(ActivityThread);//获取currentActivityThread 对象

                        Method getApplicationMethod = currentActivityThread.getClass().getMethod("getApplication");
                        getApplicationMethod.setAccessible(true);
                        CONTEXT_INSTANCE = (Context) getApplicationMethod.invoke(currentActivityThread);//获取 Context对象

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return CONTEXT_INSTANCE;
    }

    public static void setContext(Context context) {
        CONTEXT_INSTANCE = context.getApplicationContext();
    }
}
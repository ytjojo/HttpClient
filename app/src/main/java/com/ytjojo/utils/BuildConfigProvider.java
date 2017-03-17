package com.ytjojo.utils;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BuildConfigProvider {

    private static Context sContext;

    private static String packageName;

    public static String getBuildType() {
        String buildType = (String) getBuildConfigValue("BUILD_TYPE");
        if ("debug".equals(buildType)) {
            buildType = "debug";
        }
        if ("release".equals(buildType)) {
            buildType = "release";
        }
        return buildType;
    }

    public static final boolean isDebug() {
        return ((Boolean)getBuildConfigValue("DEBUG")).booleanValue();
    }

    /**
     * 通过反射获取ApplicationContext
     *
     * @return
     */
    private static Context getContext() {
        if (sContext == null) {
            try {
                final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                final Method currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread");
                final Object activityThread = currentActivityThread.invoke(null);
                final Method getApplication = activityThreadClass.getDeclaredMethod("getApplication");
                final Application application = (Application) getApplication.invoke(activityThread);
                sContext = application.getApplicationContext();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return sContext;
    }

    /**
     * 通过反射获取包名
     *
     * @return
     */
    private static String getPackageName() {
        if (packageName == null) {
            try {
                final Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
                final Method currentPackageName = activityThreadClass.getDeclaredMethod("currentPackageName");
                packageName = (String) currentPackageName.invoke(null);
            } catch (Exception e) {
                packageName = getContext().getPackageName();
            }
            packageName =checkPackageName(packageName);
        }

        return packageName;
    }
    private static String checkPackageName(String packageName) {
        String[] temp = packageName.split("\\.");
        String sub = temp[temp.length - 1];
        //如果多包共存模式，剔除包名中的后缀
        if (sub.equals("debug")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < temp.length - 1; i++) {
                sb.append(temp[i]);
                if (i != temp.length - 2) {
                    sb.append(".");
                }
            }
            packageName = sb.toString();
        }
        return packageName;
    }
    public static Object getBuildConfigValue(String fieldName) {

        try {
            Class<?> clazz = Class.forName(packageName + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return "";
    }
}
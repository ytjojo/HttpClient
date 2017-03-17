package com.ytjojo.utils;

import android.view.View;

/**
 * 系统NavBar工具类
 */
public class SystemUiVisibilityUtil {


    private static final int FLAG_IMMERSIVE = View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

    public static void enter(View decor) {

        SystemUiVisibilityUtil.addFlags(decor, FLAG_IMMERSIVE);
    }

    public static void exit(View decor) {

        SystemUiVisibilityUtil.clearFlags(decor, FLAG_IMMERSIVE);
    }

    public static void addFlags(View view, int flags) {

        view.setSystemUiVisibility(view.getSystemUiVisibility() | flags);
    }

    public static void clearFlags(View view, int flags) {

        view.setSystemUiVisibility(view.getSystemUiVisibility() & ~flags);
    }

    public static boolean hasFlags(View view, int flags) {

        return (view.getSystemUiVisibility() & flags) == flags;
    }
}
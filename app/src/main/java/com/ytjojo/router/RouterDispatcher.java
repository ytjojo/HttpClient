package com.ytjojo.router;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;

import com.ytjojo.ui.BaseActivity;

import java.util.List;

/**
 * Created by Administrator on 2016/9/19 0019.
 */
public class RouterDispatcher {

    //    <intent-filter>
//    <action android:name="android.intent.action.VIEW"/>
//    <category android:name="android.intent.category.DEFAULT"/>
//    <category android:name="android.intent.category.BROWSABLE"/>
//      <!-- 接受以"http://recipe-app.com/recipe"开头的URI -->
//    <body android:scheme="http"
//    android:host="recipe-app.com"
//    android:pathPrefix="/recipe" />
//    </intent-filter>
    protected void dispatchRout(BaseActivity activity, Bundle savedInstanceState) {
        Intent intent = activity.getIntent();
        Uri referrerUri = activity.getReferrer();
        if (referrerUri != null) {
            if (referrerUri.getScheme().equals("http") || referrerUri.getScheme().equals("https")) {
                // App从浏览器打开
                String host = referrerUri.getHost();
                // host会包含host路径 (比如www.google.com)

                // 在这里增加分析的代码以记录从Web搜索点击进来的流量


            } else if (referrerUri.getScheme().equals("android-app")) {
                // App从另一个app被打开
                String referrerPackage ="";
                if ("com.google.android.googlequicksearchbox".equals(referrerPackage)) {
                    // App从Google app被打开
                    // host会包含host路径 (比如www.google.com)

                    // 在这里增加分析的代码以记录从Google app点击进来的流量

                } else if ("com.google.appcrawler".equals(referrerPackage)) {
                    // Google的爬虫来着，别把这个算作app使用了
                }
            }
        }

    }
    public static boolean isValidUri(Context context,Uri uri){
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return !activities.isEmpty();
    }
}



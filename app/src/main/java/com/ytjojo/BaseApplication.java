package com.ytjojo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.widget.TextView;

import com.antfortune.freeline.FreelineCore;
import com.github.promeg.xlog_android.lib.XLogConfig;
import com.lody.turbodex.TurboDex;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.Settings;
import com.promegu.xlog.base.XLogMethod;
import com.ytjojo.practice.BuildConfig;
import com.ytjojo.practice.R;
import com.ytjojo.http.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends Application {
    protected static final boolean LOG = false;
    private static BaseApplication sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.getRetrofit(this);
        sInstance = this;
        List<XLogMethod> xLogMethods = new ArrayList<>();
        xLogMethods.add(new XLogMethod(TextView.class, "setText"));
        XLogConfig.config(XLogConfig.newConfigBuilder(this)
//                .logMethods(xLogMethods) //optional
//                .timeThreshold(10) // optional
                .build());

        Logger.initialize(
                Settings.getInstance()
                        .isShowMethodLink(true)
                        .isShowThreadInfo(false)
                        .setMethodOffset(0)
                        .setLogPriority(BuildConfig.DEBUG ? Log.VERBOSE : Log.ASSERT)
        );
        FreelineCore.init(this);
    }
    public static BaseApplication getInstance(){
        return sInstance;
    }
    @Override
    protected void attachBaseContext(Context base) {
        try{
            TurboDex.enableTurboDex();
            MultiDex.install(this);
        }catch (Exception e){
//            if(System.getenv("ROBOLECTRIC") == null) {
//            }
            System.out.println(System.getProperty("java.vm.name"));
            if (!System.getProperty("java.vm.name").startsWith("Java")) {
                throw e;
            }
        }

        super.attachBaseContext(base);

    }
    public boolean isLargeScreen(){
        return !getResources().getBoolean(R.bool.small_screen);
    }
}
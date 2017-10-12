package com.ytjojo;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.widget.TextView;

import com.github.promeg.xlog_android.lib.XLogConfig;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.promegu.xlog.base.XLogMethod;
import com.ytjojo.http.CookiesManager;
import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.interceptor.ReceivedCookiesInterceptor;
import com.ytjojo.practice.R;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends Application {
    protected static final boolean LOG = false;
    private static BaseApplication sInstance;
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.init(RetrofitClient.newBuilder().baseUrl("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/")
                .showLog(true).cookie(new CookiesManager(this))
                .cache(getCacheDir())
                .addInterceptor(new ReceivedCookiesInterceptor()));
        sInstance = this;
        List<XLogMethod> xLogMethods = new ArrayList<>();
        xLogMethods.add(new XLogMethod(TextView.class, "setText"));
        XLogConfig.config(XLogConfig.newConfigBuilder(this)
//                .logMethods(xLogMethods) //optional
//                .timeThreshold(10) // optional
                .build());

        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(5)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("ngr")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
    }
    public static BaseApplication getInstance(){
        return sInstance;
    }
    @Override
    protected void attachBaseContext(Context base) {
        try{
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

    @Override public void onLowMemory() {
        super.onLowMemory();
    }

    @Override public void onTerminate() {
        super.onTerminate();
    }

    @Override public void onTrimMemory(int level) {
        super.onTrimMemory(level);
    }


}
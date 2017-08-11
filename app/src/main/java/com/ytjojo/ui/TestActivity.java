package com.ytjojo.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ytjojo.practice.R;
import com.ytjojo.rx.RxCreator;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.finalteam.toolsfinal.logger.Logger;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Administrator on 2016/4/18 0018.
 */
public class TestActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.getDefaultLogger().e("oncreate");
//        getWindow().clearFlags(WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY);
        Toast.makeText(getActivy(),"crate",Toast.LENGTH_LONG).show();
//        setStatusbarColor1();
        Random random = new Random();
        boolean ss = random.nextInt(10)%2==0?true:false;
        setSetStatusbarColor2(this,getResources().getColor(R.color.colorPrimaryDark),ss);
        ViewGroup mContentView = (ViewGroup) getWindow().findViewById(Window.ID_ANDROID_CONTENT);

        RxCreator.getAsyncObservable(new IntegerThread()).observeOn(AndroidSchedulers.mainThread());
    }
    public void setStatusbarColor1(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup firstChildAtDecorView = ((ViewGroup) ((ViewGroup)getWindow().getDecorView()).getChildAt(0));
            View statusView = new View(this);
            ViewGroup.LayoutParams statusViewLp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    getStatusBarHeight());
            //颜色的设置可抽取出来让子类实现之
            statusView.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            firstChildAtDecorView.addView(statusView, 0, statusViewLp);
            firstChildAtDecorView.getChildAt(0).setFitsSystemWindows(false);
        }
    }
    public void setSetStatusbarColor2(Activity activity,int statusColor,boolean hideStatusBarBackground){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
            Window window = activity.getWindow();

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (hideStatusBarBackground) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(Color.TRANSPARENT);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }

            ViewGroup mContentView = (ViewGroup) window.findViewById(Window.ID_ANDROID_CONTENT);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                ViewCompat.setOnApplyWindowInsetsListener(mChildView, new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        return insets;
                    }
                });
                ViewCompat.setFitsSystemWindows(mChildView, false);
                ViewCompat.requestApplyInsets(mChildView);

            }
            ViewGroup firstChildAtDecorView = ((ViewGroup) ((ViewGroup)getWindow().getDecorView()).getChildAt(0));
            com.orhanobut.logger.Logger.e(" 是否相等" + (mContentView == firstChildAtDecorView.getChildAt(0)));
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    public void changStatusTextIconColor(boolean lightStatusBar){
        Window window = getWindow();
        View decor = window.getDecorView();
        int ui = decor.getSystemUiVisibility();
        if (lightStatusBar) {
            ui |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            ui &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(ui);
        // 去掉系统状态栏下的windowContentOverlay
        View v = window.findViewById(android.R.id.content);
        if (v != null) {
            v.setForeground(null);
        }
    }


    private int getStatusBarHeight() {
        int resId = getResources().getIdentifier("status_bar_height","dimen","android");
        if(resId>0){
            return getResources().getDimensionPixelSize(resId);
        }
        return 0;
    }
    public static class IntegerThread extends   RxCreator.EventSource<Integer>{
        Thread mThread;
        AtomicBoolean mAtomicBoolean = new AtomicBoolean(true);
        int mInt =0;
        @Override
        public void onStart() {
            Runnable runnable =new Runnable() {
                @Override
                public void run() {
                    while (true &&mAtomicBoolean.get()){
                        onDelieveryEvent(mInt++);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            mThread = new Thread(runnable,"IntegerThread");
            mThread.start();
        }

        @Override
        public void onStop() {
            mAtomicBoolean.set(false);
            onStopClear();
        }
    }

    @Override
    public Class<? extends BaseFragment> getRootFragmentClass() {
        return Fragment1.class;
    }

    @Override
    public boolean canSwipBack() {
        return true;
    }
}

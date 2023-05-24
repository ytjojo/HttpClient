package com.jiulongteng.http.demo;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.jiulongteng.http.download.DownloadListener;
import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.FileTargetProvider;
import com.jiulongteng.http.download.SpeedCalculator;
import com.jiulongteng.http.download.SpeedListener;
import com.jiulongteng.http.download.TargetProvider;
import com.jiulongteng.http.download.UriTargetProvider;
import com.jiulongteng.http.download.Util;
import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.Dao;
import com.jiulongteng.http.download.db.DownloadCache;
import com.jiulongteng.http.interceptor.HttpLoggingInterceptor;

import java.io.File;
import java.net.URLConnection;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    EditText etUrl;
    TextView tvDownLoad;
    ProgressBar horizontalBar;
    TextView tvPercent;

    DownloadTask downloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivity_main);
        tvDownLoad = findViewById(R.id.tv_download);
        etUrl = findViewById(R.id.et_url);
        horizontalBar = findViewById(R.id.pb_horizontal);
        tvPercent = findViewById(R.id.tv_percent);
        tvDownLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
        DownloadCache.setContext(getApplicationContext());


        insertApk();
    }
    @TargetApi(Build.VERSION_CODES.Q)
    private void insertApk(){


    }

    long start;
    long stop;

    public void download(){
        if(downloadTask == null){
//            String url = "http://update.myweimai.com/wemay.apk";
            String url = "https://cdn.llscdn.com/yy/files/xs8qmxn8-lls-LLS-5.8-800-20171207-111607.apk";
//            String url = "http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe";

            Request request = new Request.Builder().url(url)
                    .build();
            Util.setLogger(new Util.Logger() {
                @Override
                public void e(String tag, String msg, Throwable e) {
                    Log.e(tag , msg + e.getMessage());
                }

                @Override
                public void w(String tag, String msg) {
                    Log.w(tag ,  msg );
                }

                @Override
                public void d(String tag, String msg) {
                    Log.d(tag , msg );
                }

                @Override
                public void i(String tag, String msg) {
                    Log.i(tag ,  msg );
                }
            });
            HttpLoggingInterceptor loginter = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Util.d("http",message);
                }
            });
            OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(loginter).build();
            TargetProvider targetProvider = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                targetProvider = new UriTargetProvider(MediaStore.Downloads.EXTERNAL_CONTENT_URI,"myapk2.apk",null);
            }else {
                targetProvider = new FileTargetProvider(getExternalCacheDir());
            }
            targetProvider = new FileTargetProvider(getExternalCacheDir());
            downloadTask = new DownloadTask(targetProvider,okHttpClient,request,5);

            downloadTask.setDownloadListener(new DownloadListener() {
                @Override
                public void taskStart(@androidx.annotation.NonNull DownloadTask task) {
                    Util.i(TAG,"taskStart");
                    start = System.currentTimeMillis();
                    stop = start;
                }

                @Override
                public void connectTrialStart(@androidx.annotation.NonNull DownloadTask task) {
                    Util.i(TAG,"connectTrialStart");
                }

                @Override
                public void fetchStart(@androidx.annotation.NonNull DownloadTask task, boolean isFromBeginning) {
                    Util.i(TAG,"fetchStart"+task.getTargetProvider().getTargetFile().getAbsolutePath() +" totalOffset" + task.getInfo().getTotalOffset()  +" getTotalLength " + task.getInfo().getTotalLength());
                }

                @Override
                public void fetchProgress(@androidx.annotation.NonNull DownloadTask task, int currentProgress, long currentSize, long contentLength,long speed) {
                    horizontalBar.setProgress(currentProgress);
                    tvPercent.setText(currentProgress + "%  currentSize =" + currentSize  + " speed " +speed );
                    Util.i(TAG,"fetchProgress" + currentProgress + " currentSize "+ currentSize + " contentLength " + contentLength);
                }

                @Override
                public void taskEnd(@androidx.annotation.NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Throwable realCause) {
                    if(realCause != null){
                        realCause.printStackTrace();
                        Util.e(TAG,"error" ,realCause);
                    }
                    Util.i(TAG,cause.toString() +"  taskEnd  stop =" + (System.currentTimeMillis()   - stop)  + "  total = " +  + (System.currentTimeMillis() - start));
                }
            });
            downloadTask.setSpeedListener(new SpeedListener() {
                @Override
                public void onProgress(DownloadTask task, SpeedCalculator speedCalculator) {
                    Util.i(TAG,"averageSpeed" + speedCalculator.averageSpeed() + "  "+ speedCalculator.speedFromBegin()+ "  "+ speedCalculator.instantSpeed());
                }
            });
        }

        if(downloadTask.getTaskStatus() == DownloadCache.RUNNING){
            downloadTask.stop();
        }else {
            downloadTask.enqueue();
        }


    }
}

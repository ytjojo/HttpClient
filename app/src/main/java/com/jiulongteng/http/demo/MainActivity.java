package com.jiulongteng.http.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jiulongteng.http.download.DownloadListener;
import com.jiulongteng.http.download.DownloadTask;
import com.jiulongteng.http.download.Util;
import com.jiulongteng.http.download.cause.EndCause;
import com.jiulongteng.http.download.db.Dao;
import com.jiulongteng.http.download.db.DownloadCache;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity";
    EditText etUrl;
    TextView tvDownLoad;
    ProgressBar horizontalBar;
    TextView tvPercent;

    DownloadTask downloadTask;

    boolean continueDownload;
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
                continueDownload = true;
            }
        });
        Dao.init(getApplicationContext());
        DownloadCache.getInstance().setBreakpointStore(Dao.getInstance());

    }

    long start;
    long stop;

    public void download(){
        if(downloadTask == null){
            Request request = new Request.Builder().url("http://dldir1.qq.com/weixin/Windows/WeChatSetup.exe")
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
            downloadTask = new DownloadTask(getExternalCacheDir(),new OkHttpClient(),request,5);

            downloadTask.setDownloadListener(true, new DownloadListener() {
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
                    Util.i(TAG,"fetchStart"+task.getFile().getAbsolutePath() +" getTotalLength " + task.getInfo().getTotalLength());
                }

                @Override
                public void fetchProgress(@androidx.annotation.NonNull DownloadTask task, int currentProgress, long currentSize, long contentLength) {
                    horizontalBar.setProgress(currentProgress);
                    tvPercent.setText(currentProgress + "%");
                    Util.i(TAG,"fetchProgress" + currentProgress + " currentSize "+ currentSize + " contentLength " + contentLength);
                    if(!continueDownload && currentProgress > 10){
                        Util.i(TAG,"----------stop");
                        task.stop();
                        stop = System.currentTimeMillis();
                        Util.i(TAG,"----------after stop");
                    }
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
        }

        if(downloadTask.getTaskStatus() == DownloadCache.RUNNING){
            downloadTask.stop();
        }else {
            downloadTask.enqueue();
        }


    }
}

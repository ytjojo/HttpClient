package com.jiulongteng.http.progress;


import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.jiulongteng.http.download.db.DownloadCache;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

//上传文件，带进度的请求实体
public class ProgressRequestBody extends RequestBody {

    //实际的待包装请求体
    private final RequestBody requestBody;
    //进度回调接口
    private final ProgressListener callback;
    private long lastCallbackProcessTime;
    //包装完成的BufferedSink
    private BufferedSink bufferedSink;
    private Handler handler;

    private long bytesWritten = 0L;
    //总字节长度，避免多次调用contentLength()方法
    private long contentLength = 0L;

    /**
     * 构造函数，赋值
     *
     * @param requestBody 待包装的请求体
     * @param callback    回调接口
     */
    public ProgressRequestBody(RequestBody requestBody, ProgressListener callback) {
        this.requestBody = requestBody;
        this.callback = callback;
        handler = DownloadCache.getInstance().isAndroid() ? new Handler(Looper.getMainLooper()) : null;
    }

    public RequestBody getRequestBody() {
        return requestBody;
    }

    /**
     * 重写调用实际的响应体的contentType
     *
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     *
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        try{
            return requestBody.contentLength();
        }catch (Exception e){
            return -1;
        }

    }

    /**
     * 重写进行写入
     *
     * @param sink BufferedSink
     * @throws IOException 异常
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        //此行代码为兼容添加 HttpLoggingInterceptor 拦截器后，上传进度超过100%，达到200%问题
        if (sink instanceof Buffer) return;
        if (bufferedSink == null) {
            //包装
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();

    }

    /**
     * 写入，回调进度接口
     *
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数


            int lastProgress; //上次回调进度

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                    lastCallbackProcessTime = nowMillis();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;

                int currentProgress = (int) ((bytesWritten * 100) / contentLength);
                lastProgress = currentProgress;

                if(isDispatchProcessMoment()){
                    //回调, 更新进度
                    updateProgress(lastProgress, bytesWritten, contentLength);
                }

            }
        };
    }

    private void updateProgress(int progress, long currentSize, long totalSize) {
        if (callback == null) return;
        if(handler != null){
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.update(progress, currentSize, totalSize);
                }
            });
        }else {
            callback.update(progress, currentSize, totalSize);
        }

    }
    private long nowMillis() {
        if (DownloadCache.getInstance().isAndroid()) {
            return SystemClock.uptimeMillis();
        }
        return System.nanoTime() / 1000000;
    }

    public boolean isDispatchProcessMoment() {

        if(contentLength > 0 && contentLength == bytesWritten){
            return true;
        }

        final long minInterval = DownloadCache.getInstance().getProgressDispatchInterval();
        final long now = nowMillis();
        return minInterval <= 0
                || now - getLastDispatchProcessTime() >= minInterval;
    }

    public long getLastDispatchProcessTime() {
        return lastCallbackProcessTime;
    }
}

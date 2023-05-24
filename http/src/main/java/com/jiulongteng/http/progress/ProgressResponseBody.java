package com.jiulongteng.http.progress;


import android.os.Handler;
import android.os.Looper;

import com.jiulongteng.http.download.db.DownloadCache;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

public class ProgressResponseBody extends ResponseBody {

    private final ResponseBody responseBody;
    private final ProgressListener progressListener;
    private BufferedSource bufferedSource;
    private Handler handler;

    private long totalBytesRead = 0L;
    //总字节长度，避免多次调用contentLength()方法
    private long contentLength = 0L;

    ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
        handler = DownloadCache.getInstance().isAndroid() ? new Handler(Looper.getMainLooper()) : null;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        try {
            return responseBody.contentLength();
        } catch (Exception e) {
            return -1;
        }

    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {


            int lastProgress; //上次回调进度

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                int currentProgress = (int) ((totalBytesRead * 100) / contentLength);
                if (currentProgress <= lastProgress) return bytesRead; //进度较上次没有更新，直接返回
                lastProgress = currentProgress;
                progressListener.update(currentProgress, totalBytesRead, contentLength);
                lastProgress = currentProgress;
                return bytesRead;
            }
        };
    }
}



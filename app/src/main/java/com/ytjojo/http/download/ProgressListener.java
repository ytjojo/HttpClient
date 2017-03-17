package com.ytjojo.http.download;

/**
 * 下载进度listener
 * Created by JokAr on 16/5/11.
 */
public interface ProgressListener {
    void onProgress(long bytesRead, long contentLength, boolean done);
}
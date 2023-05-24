package com.jiulongteng.http.progress;


public interface ProgressListener {
    void update(int progress, long currentSize, long contentLength);
}
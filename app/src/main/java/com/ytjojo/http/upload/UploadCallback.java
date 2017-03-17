package com.ytjojo.http.upload;

/**
 * Created by Administrator on 2016/11/20 0020.
 */
public abstract class UploadCallback {
    long totalLength;
    long FinishedFilesLength;
    int finishedFileCount;

    abstract void onProgress(String percent,long readBytes,long totalLength,int finisedCount);


}

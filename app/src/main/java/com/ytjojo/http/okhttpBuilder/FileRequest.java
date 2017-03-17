package com.ytjojo.http.okhttpBuilder;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2016/12/18 0018.
 */
public class FileRequest {
    File mFile;
    public FileRequest(File file){
        mFile =file;
    }
    public FileRequest post(MediaType mediaType){
        RequestBody.create(mediaType, mFile);
        return this;
    }
}

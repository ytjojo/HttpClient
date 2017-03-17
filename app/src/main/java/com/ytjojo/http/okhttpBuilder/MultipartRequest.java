package com.ytjojo.http.okhttpBuilder;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2016/12/18 0018.
 */
public class MultipartRequest {
    MultipartBody.Builder mMultipartBulder;
    private MultipartRequest(){
        mMultipartBulder =new MultipartBody.Builder();
        mMultipartBulder.setType(MultipartBody.FORM);

    }
    public static MultipartRequest create(){
       return new MultipartRequest();
    }
    public MultipartRequest file(String key,File file){
        RequestBody fileBody = RequestBody.create(MediaTypeBuilder.getMediaType(file), file);
        mMultipartBulder.addFormDataPart(key,file.getName(),fileBody);
//        mMultipartBulder.addPart(
//                Headers.of("Content-Disposition", "form-data; name=\""+ key +"\""),
//                RequestBody.create(MediaTypeBuilder.getMediaType(file), file));
        return this;
    }
    public MultipartRequest image(File file){
        RequestBody fileBody = RequestBody.create(MediaTypeBuilder.getMediaType(file), file);
        mMultipartBulder.addFormDataPart("image",file.getName(),fileBody);
//        mMultipartBulder.addPart(
//                Headers.of("Content-Disposition", "form-data; name=\""+ key +"\""),
//                RequestBody.create(MediaTypeBuilder.getMediaType(file), file));
        return this;
    }
    public MultipartRequest param(String key,String value){
        mMultipartBulder.addFormDataPart(key,value);
//        mMultipartBulder.addPart(Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
//                RequestBody.create(null, value));
        return this;
    }
    public MultipartRequest file(String key,String name,File file){
        RequestBody fileBody = RequestBody.create(MediaTypeBuilder.getMediaType(file), file);
        mMultipartBulder.addFormDataPart(key,name,fileBody);
        return this;
    }
    public MultipartRequest file(String key,String name,File file,MediaType mediaType){
        RequestBody fileBody = RequestBody.create(mediaType, file);
        mMultipartBulder.addFormDataPart(key,name,fileBody);
        return this;
    }
}

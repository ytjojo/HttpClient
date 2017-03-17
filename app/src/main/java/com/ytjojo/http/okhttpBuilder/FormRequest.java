package com.ytjojo.http.okhttpBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2016/12/17 0017.
 */
public class FormRequest {
    HashMap<String,String> mHashMap;
    Type mResponseType;
    CacheControl mCacheControl;
    public FormRequest(){
        mHashMap = new HashMap<>();
    }
    public static FormRequest create(){

        return new FormRequest();
    }
    public FormRequest add(String key,String value){
        mHashMap.put(key,value);
        return this;
    }
    public FormRequest add(HashMap<String,String> params){

        mHashMap.putAll(params);
        return this;
    }
    public FormRequest cacheControl(CacheControl cacheControl){

        this.mCacheControl = cacheControl;
        return this;
    }
    public FormRequest post(Type type){
        mResponseType = type;
        FormBody.Builder formBuilder = new FormBody.Builder();
        for(Map.Entry<String,String> entry:mHashMap.entrySet()){
            formBuilder.addEncoded(entry.getKey(),entry.getValue());

        }
       RequestBody body =  formBuilder.build();
        Request.Builder builder = new Request.Builder();
        if(mCacheControl!=null){
            builder.cacheControl(mCacheControl);
        }

        builder.url("").post(body).build();


        return this;
    }
}

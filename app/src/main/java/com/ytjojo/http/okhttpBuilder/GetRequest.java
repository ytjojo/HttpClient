package com.ytjojo.http.okhttpBuilder;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Created by Administrator on 2016/12/18 0018.
 */
public class GetRequest {
    String mUrl;
    HashMap<String,String> mParams;
    HttpUrl.Builder mUriBuilderNoQuery;
    long cacheSecond;
    private GetRequest(String url){
        mUrl = url;
        HttpUrl httpUrl = HttpUrl.parse(mUrl);
        mParams = new HashMap<>();
        mUriBuilderNoQuery = httpUrl.newBuilder();

    }
    public GetRequest cacheAge(long cacheSecond){
        this.cacheSecond = cacheSecond;
        return this;
    }
    public static GetRequest create(String url){
        return new GetRequest(url);
    }
    public GetRequest add(String key, String value){
        mParams.put(key,value);
        return this;
    }
    public GetRequest add(Map<String,String> map){
        mParams.putAll(map);
        return this;
    }
    public GetRequest get(Type type){
        for(Map.Entry<String,String> entry:mParams.entrySet()){
            mUriBuilderNoQuery.setQueryParameter(entry.getKey(),entry.getValue());
        }
        Request.Builder builder = new Request.Builder();
        Request request = builder.url( mUriBuilderNoQuery.build().toString())
                .get()
                .build();
        return this;
    }
}

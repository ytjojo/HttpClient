package com.ytjojo.videoHttp;

import android.os.Handler;
import android.os.Looper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.ytjojo.http.OkHttpClientBuilder;
import com.ytjojo.utils.TextUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.ytjojo.videoHttp.JsonRequestBody.mapper;

/**
 * Created by Administrator on 2016/12/3 0003.
 */
public class ResponseDispatcher {
    public static final String BASE_POST_URL = "";
    TypeReference<ResponseWraper> mTypeReference;
    Callback mCallback;
    RequestBody mRequestBody;
    JavaType mBodyJavaType;
    Call mHttpCall;
    private String mTag;
    private boolean cancelOnRemoveCallback;
    private OkHttpClient mOkHttpClient;

    public ResponseDispatcher(RequestBody requestBody){
        this.mRequestBody = requestBody;
        looper(Looper.getMainLooper());
    }

    private Handler mHandler;

    public ResponseDispatcher looper(Looper looper){
        mHandler = new Handler(looper);
        return this;
    }

    public ResponseDispatcher callImmediately(){
        mHandler=null;
        return this;
    }

    /**
     *
     * @param reference
     * 如果body对应的ArrayList<Doctor>
     *     TypeReference<ResponseWraper<ArrayList<Dcotor>>> typeRef = new TypeReference<ResponseWraper<ArrayList<Dcotor>>>(){};
     *     注意创建的代码有大括号
     *     bodyType
     * @return
     */
    ResponseDispatcher responseType(TypeReference reference){
        this.mTypeReference = reference;
        return this;
    }

    /**
     *
     * @param bodyJavaType json 中body对应的java类型如ArrayList<Doctor>
     *                     mapper.getTypeFactory().constructParametrizedType(ArrayList.class, HArrayList.class,Doctor.class);
     * @return
     */
    ResponseDispatcher bodyType(JavaType bodyJavaType){
        this.mBodyJavaType = bodyJavaType;
        return this;
    }

    /**
     * 同步的请求方法,需要自己捕获异常,同时判断结果null的情况
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T excute() throws Exception {
        Request.Builder builder = new Request.Builder();
        if(!TextUtils.isEmpty(mTag)){
            builder.tag(mTag);
        }
        Request request = builder.url(BASE_POST_URL)
                .post(mRequestBody).build();
        ResponseBody body = null;
        try {
            mHttpCall =getClient().newCall(request);

            Response response = mHttpCall.execute();
            if (response.isSuccessful()) {
                body = response.body();
                ResponseWraper<T> wraper =null;
                if (mBodyJavaType != null) {
                    JavaType javaType = mapper.getTypeFactory().constructParametrizedType(ResponseWraper.class, ResponseWraper.class, mBodyJavaType);
                    ObjectReader reader = mapper.reader(javaType);
                    wraper = reader.readValue(body.charStream());
                } else if (mTypeReference != null) {
                    wraper = mapper.readValue(body.charStream(), mTypeReference);
                }
                if(wraper!=null)
                return wraper.body;
            }

        } catch (IOException e) {
            throw  new RuntimeException(e);
        } catch (Exception e){
           throw  new RuntimeException(e);
        } finally {
            if (body != null) {
                body.close();
            }
        }
        return null;
    }

    /**
     * 默认回调主线程,如果需要回调后台线程先调用callImmediately();
     * 或者自己指定某个线程looper();把那个线程的looper对象传进去
     * @param releaser
     * @param callback
     * @param <T>
     */
    public <T> void enqueue(CallbackReleaser releaser,Callback<T> callback) {
        if(releaser !=null){
            releaser.add(this);
        }
        this.mCallback = callback;
        Request.Builder builder = new Request.Builder();
        if(!TextUtils.isEmpty(mTag)){
            builder.tag(mTag);
        }
        Request request = builder.url(BASE_POST_URL)
                .post(mRequestBody).build();
        mHttpCall =getClient().newCall(request);
        mHttpCall.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (mCallback != null)
                    mCallback.onFail(0, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (mCallback == null) {
                    return;
                }
                if (response.isSuccessful()) {
                    ResponseWraper<T> wraper = null;
                    ResponseBody requestBody = response.body();
                    try {
                        if (mBodyJavaType != null) {
                            JavaType javaType = mapper.getTypeFactory().constructParametrizedType(ResponseWraper.class, ResponseWraper.class, mBodyJavaType);
                            ObjectReader reader = mapper.readerFor(javaType);
                            wraper = reader.readValue(requestBody.charStream());
                        } else if (mTypeReference != null) {
                            wraper = mapper.readValue(requestBody.charStream(), mTypeReference);
                        }
                        if (wraper != null) {
                            if (wraper.code == 200) {
                                onSuccsess(wraper.body);
                            } else {
                                onError(wraper.code, wraper.msg);
                            }
                        } else {
                            onError(-15, "json parse error");
                        }
                    } finally {
                        requestBody.close();
                    }

                } else {
                    onError(response.code(), response.message());
                }
            }
        });
    }

    private OkHttpClient getClient() {
        return null;
    }

    private <T> void onSuccsess(final T t){
        if(mCallback ==null){
            return;
        }
        if(mHandler !=null){
            mHandler.post(new Runnable() {
                              @Override
                              public void run() {
                                  if (mCallback != null)
                                      mCallback.onSuccsess(t);
                              }
                          }
            );
        }else{
            if (mCallback != null)
                mCallback.onSuccsess(t);
        }

    }
    private void onError(final int code,final String message){
        if(mCallback ==null){
            return;
        }
        if(mHandler !=null){
            mHandler.post(new Runnable() {
                              @Override
                              public void run() {
                                  if (mCallback != null)
                                      mCallback.onFail(code, message);
                              }
                          }
            );
        }else{
            if (mCallback != null)
                mCallback.onFail(code, message);
        }
    }

    public ResponseDispatcher tag(String tag){
        this.mTag = tag;
        return this;
    }
    public ResponseDispatcher cancelOnRemoveCallback(){
        cancelOnRemoveCallback = true;
        return this;
    }
    public void removeCallback() {
        if(cancelOnRemoveCallback&&mHttpCall !=null){
            cancel();
        }
        mTypeReference = null;
        mCallback = null;
        mRequestBody = null;
        mBodyJavaType = null;
        mHttpCall = null;

    }


    public void cancel() {
        if(mHttpCall !=null&&mHttpCall.isExecuted()&&!mHttpCall.isCanceled()){
            mHttpCall.cancel();
        }
    }

    /**
     * 建议不要每个请求随意更改这个参数,每次更改会创建新的Client,浪费资源
     * @param connect
     * @param read$write
     * @return
     */
    public ResponseDispatcher connectTimeout(int connect,int read$write){
        mOkHttpClient= OkHttpClientBuilder.builder(null).connectTimeout(connect, TimeUnit.SECONDS).readTimeout(read$write,TimeUnit.SECONDS)
        .writeTimeout(read$write,TimeUnit.SECONDS).build();
        return this;
    }

    /**
     * 不是必须的,极特殊的情况下才会用到
     * @param client
     * @return
     */
    public ResponseDispatcher client(OkHttpClient client){
        this.mOkHttpClient = client;
        return this;
    }
}

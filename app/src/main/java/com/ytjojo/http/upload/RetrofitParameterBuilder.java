package com.ytjojo.http.upload;

import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;
import rx.Observable;

public class RetrofitParameterBuilder {
    private static RetrofitParameterBuilder mParameterBuilder;  
    private static Map<String, RequestBody> params;
  
    /** 
     * 构建私有方法 
     */  
    private RetrofitParameterBuilder() {  
  
    }  
  
    /** 
     * 初始化对象 
     */  
    public static RetrofitParameterBuilder newBuilder(){  
        if (mParameterBuilder==null){  
            mParameterBuilder = new RetrofitParameterBuilder();  
            if (params==null){  
                params = new HashMap<>();
            }  
        }  
        return mParameterBuilder;  
    }  
  
    /** 
     * 添加参数 
     * 根据传进来的Object对象来判断是String还是File类型的参数 
     */  
    public RetrofitParameterBuilder addParameter(String key, File file) {
        RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);
        params.put(key + "\"; filename=\"" + file.getName() + "", body);
        return this;  
    }  
  
    /**
     * 添加参数
     * 根据传进来的Object对象来判断是String还是File类型的参数
     */
    public RetrofitParameterBuilder addParameter(String key, String value) {
        RequestBody body = RequestBody.create(MediaType.parse("text/plain"), value);
        params.put(key, body);
        return this;
    }

    /**
     * 初始化图片的Uri来构建参数  
     * 一般不常用  
     * 主要用在拍照和图库中获取图片路径的时候  
     */  
    public RetrofitParameterBuilder addFilesByUri(String key, List<Uri> uris) {
  
        for (int i = 0; i < uris.size(); i++) {  
            File file = new File(uris.get(i).getPath());  
            RequestBody body = RequestBody.create(MediaType.parse("image/*"), file);  
            params.put(key + i + "\"; filename=\"" + file.getName() + "", body);  
        }  
  
        return this;  
    }  
  
    /**  
     * 网络请求完成之后，别忘了在回调函数中调一下这个方法  
     * 如果你用的是RxJava，可以再onCompleted和onError中调一下  
     * 如果不清空，可能会出现一个问题，就是下一次的网络请求回带有上次网络请求的参数  
     * 因为我这里创建的构造参数的方法是类似于单例，实例不会再次创建  
     * 这里是需要注意的地方  
     */  
    public RetrofitParameterBuilder cleanParams(){
        if (params!=null){  
            params.clear();  
        }
        return this;
    }

    /**
     *
     * @param params
     * @param fileKey image or file
     * @param file
     * @return
     */
    public static RequestBody requestBody(HashMap<String,String> params,String fileKey,File file){
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
                for(Map.Entry<String,String> entry:params.entrySet()){
                    builder.addFormDataPart(entry.getKey(), entry.getValue());
                }
                builder.addFormDataPart(fileKey, file.getName(), RequestBody.create(MediaType.parse("image/*"), file));

        return builder.build();
    }

    public static MultipartBody filesToMultipartBody(List<File> files) {
        MultipartBody.Builder builder = new MultipartBody.Builder();

        for (File file : files) {
            // TODO: 16-4-2  这里为了简单起见，没有判断file的类型
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
            builder.addFormDataPart("file", file.getName(), requestBody);
        }

        builder.setType(MultipartBody.FORM);
        MultipartBody multipartBody = builder.build();
        return multipartBody;
    }

    public static List<MultipartBody.Part> filesToMultipartBodyParts(List<File> files) {
        List<MultipartBody.Part> parts = new ArrayList<>(files.size());
        for (File file : files) {
            // TODO: 16-4-2  这里为了简单起见，没有判断file的类型
            RequestBody requestBody = RequestBody.create(MediaType.parse("image/png"), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            parts.add(part);
        }
        return parts;
    }
    public static MultipartBody.Part getMultipartBodyPart(File file){
        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        // MultipartBody.Part  和后端约定好Key，这里的partName是用image
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("image", file.getName(), requestFile);
        return body;
    }
    public static interface UploadService{

        @POST()
        Observable<ResponseBody> upLoad(
                @Url String url,
                @Body RequestBody Body);

        //图片上传
        @Multipart
        @POST()
        Observable<String> updateImage( @Url String url,@PartMap Map<String,RequestBody> params);

        @Multipart
        @POST
        Observable<ResponseBody> uploadFileWithPartMap(
                @Url() String url,
                @PartMap() Map<String, RequestBody> partMap,
                @Part MultipartBody.Part file);

        /**
         * 通过 List<MultipartBody.Part> 传入多个part实现多文件上传
         * @param parts 每个part代表一个
         * @return 状态信息
         */
        @Multipart
        @POST("users/image")
        Observable<ResponseBody> uploadFilesWithParts(@Part() List<MultipartBody.Part> parts);


        /**
         * 通过 MultipartBody和@body作为参数来上传
         * @param multipartBody MultipartBody包含多个Part
         * @return 状态信息
         */
        @POST("users/image")
        Observable<ResponseBody> uploadFileWithRequestBody(@Body MultipartBody multipartBody);

    }
}  
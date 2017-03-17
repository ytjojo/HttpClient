package com.ytjojo.http.upload;

import com.ytjojo.http.RetrofitClient;
import com.ytjojo.http.download.ProgressListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by Administrator on 2016/11/20 0020.
 */
public class UploadHelper {
    private MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");
    private MediaType dataMediaType = MediaType.parse("multipart/form-data");
    public  Observable<String> upload(ArrayList<File> files, String url,String des,UploadCallback callback){
        HashMap<String, RequestBody> params = new HashMap<>();
        for(File file:files){
            RequestBody body =
                    RequestBody.create(dataMediaType, file);
            ProgressRequestBody requestBody =new ProgressRequestBody(body, new ProgressListener() {
                @Override
                public void onProgress(long bytesRead, long contentLength, boolean done) {
                    if (done) {
                        callback.FinishedFilesLength +=contentLength;
                        callback.finishedFileCount++;
                        callback.onProgress("100%",callback.FinishedFilesLength,callback.totalLength,callback.finishedFileCount);
                    }else{
                        callback.onProgress(bytesRead*100/contentLength+"%",callback.FinishedFilesLength +bytesRead,callback.totalLength,callback.finishedFileCount);

                    }
                }
            });
            callback.totalLength+=file.length();
            params.put("file[]\"; filename=\"" + file.getName(), requestBody);
        }
        UploadService serverApi= RetrofitClient.getRetrofit().create(UploadService.class);
        return serverApi.uploadFile(url,des, params);
    }
    public interface UploadService{
        @Multipart
        @POST("{path}")
        Observable<String> uploadFile(
                @Path(value = "path", encoded = true) String url,@Part("filedes") String des,
                @PartMap() Map<String, RequestBody> maps);
    }

}

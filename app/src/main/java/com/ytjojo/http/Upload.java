package com.ytjojo.http;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by Administrator on 2017/8/3 0003.
 */

public class Upload {

	public void uploadByPartmap(String token,String catalog, int mode, String id, File file, HashMap<String,String> params){

		RequestBody catalogRB = RequestBody.create(null, catalog);
		RequestBody idRB = RequestBody.create(null, id);
		RequestBody modeRB = RequestBody.create(MediaType.parse("text/plain"), mode+"");
		Map<String, RequestBody> requestBodyMap = new HashMap<>();
		for(Map.Entry<String,String> entry: params.entrySet()){
			RequestBody value =RequestBody.create(null, entry.getValue());
			requestBodyMap.put(entry.getKey(),value);
		}
		//        RequestBody requestFile =
		//                RequestBody.create(MediaType.parse("application/otcet-stream"), file);
		MultipartBody.Part fileBody = MultipartBody.Part.createFormData(
				"file",
				file.getName(),
				RequestBody.create(MediaType.parse("image/*"), file));
		RetrofitClient.getDefault().create(GitApiInterface.class).uploadImage(token,catalogRB,idRB,modeRB,fileBody);
	}
	public void upload( File file, HashMap<String,String> params){
		Map<String, RequestBody> requestBodyMap = new HashMap<>();
		for(Map.Entry<String,String> entry: params.entrySet()){
			RequestBody value = RequestBody.create(MediaType.parse("multipart/form-body"), entry.getValue());
			requestBodyMap.put(entry.getKey(),value);
		}
		String fileName = "file\"; filename=\"" + file.getName();
		requestBodyMap.put(fileName, RequestBody.create(MediaType.parse("multipart/form-body"), file));
		RetrofitClient.getDefault().create(GitApiInterface.class).uploadImagePartMap(requestBodyMap);
	}
}

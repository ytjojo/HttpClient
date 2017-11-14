package com.ytjojo.practice;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.http.Uri;
import java.io.IOException;
import org.junit.Test;

/**
 * Created by Administrator on 2017/7/24 0024.
 */

public class UriTest {

	@Test
	public void testuri(){
			String s= "http://www.baidu.com/user?un=123232&name=ss&sex={sex}&age=20";
			Uri uri =Uri.parse(s);
			String un  =uri.getQueryParameter("un");
			System.out.println(un);
			String name  =uri.getQueryParameter("name");
			System.out.println(name);
			uri =uri.replaceQueryParameter("sex","111").build();
			System.out.println(uri.getQueryParameter("sex"));
			//uri = uri.buildUpon().appendQueryParameter("abc","人民日报").build();
			//System.out.println(uri.getQueryParameter("abc"));
			System.out.println(uri.toString());




	}
	@Test
	public void gsontest() throws IOException {
		Gson gson = new Gson();
		toJsonArray(gson.toJsonTree(2),gson.toJsonTree(true),gson.toJsonTree("absdw"),gson.toJsonTree(new LoginRequest()));
		toJsonObject(gson.toJsonTree(2),gson.toJsonTree(true),gson.toJsonTree("absdw"),gson.toJsonTree(new LoginRequest()));

	}
	public static void toJsonArray(JsonElement ...  jes){
		JsonArray jsonArray = new JsonArray();
		for (JsonElement je:jes){
			jsonArray.add(je);
		}
		System.out.println(jsonArray.toString());
	}
	public static void toJsonObject(JsonElement ...  jes){
		JsonObject jsonObject = new JsonObject();
		for (JsonElement je:jes){
			jsonObject.add("1",je);
		}
		System.out.println(jsonObject.toString());
	}
}

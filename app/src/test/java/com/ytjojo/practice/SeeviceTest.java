package com.ytjojo.practice;

import com.google.gson.JsonObject;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.GitApiInterface;
import com.ytjojo.http.coverter.GsonConverterFactory;
import com.ytjojo.http.interceptor.HttpLoggingInterceptor;
import com.ytjojo.http.interceptor.ReceivedCookiesInterceptor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Request;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import retrofit2.ProxyHandler;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;

/**
 * Created by Administrator on 2017/7/24 0024.
 */

public class SeeviceTest {
	Retrofit retrofit;

	@Before
	public void setUp() throws Exception {
		HttpLoggingInterceptor logger = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
			@Override public void log(String message) {
				System.out.println(message);
			}
		});
		logger.setLevel( HttpLoggingInterceptor.Level.BODY);
		OkHttpClient o = new OkHttpClient.Builder().addInterceptor(logger).addInterceptor(new ReceivedCookiesInterceptor()).build();

		retrofit = new Retrofit.Builder()
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.client(o)
				.baseUrl("https://baseapi.ngarihealth.com/ehealth-base/").build();
		System.out.println("setUp");

	}
	@Test
	public void login(){
		LoginRequest request = new LoginRequest();

		retrofit.create(GitApiInterface.class).login(request).subscribe(new Subscriber<LoginResponse>() {
			@Override public void onCompleted() {

			}

			@Override public void onError(Throwable e) {

			}

			@Override public void onNext(LoginResponse loginResponse) {
				System.out.println(loginResponse.body.getDisplayName());
				ArrayList<Integer> integers = new ArrayList<Integer>();
				integers.add( loginResponse.body.getId());
				ProxyHandler.create(retrofit,GitApiInterface.class)
				.getPatientNumByHeader(integers).subscribe(new Subscriber<JsonObject>() {
					@Override public void onCompleted() {
						System.out.println("onCompleted");
					}

					@Override public void onError(Throwable e) {
						System.out.println("Throwable");
						e.printStackTrace();
						System.out.println(e.getMessage());
					}

					@Override public void onNext(JsonObject jsonpObject) {
						System.out.println(" onNext" +jsonpObject.toString());
						System.out.println("onNext");
					}
				});

			}
		});
	}

	@Test
	public void tess(){
		int ss =1425;
		ArrayList<Integer> integers = new ArrayList<Integer>();
		integers.add(ss);
		ProxyHandler.create(retrofit,GitApiInterface.class)
				.getPatientNumByHeader(integers).subscribe(new Subscriber<JsonObject>() {
			@Override public void onCompleted() {
				System.out.println("onCompleted");
			}

			@Override public void onError(Throwable e) {
				System.out.println("Throwable");
				e.printStackTrace();
				System.out.println(e.getMessage());
			}

			@Override public void onNext(JsonObject jsonpObject) {
				System.out.println(" onNext" +jsonpObject.toString());
				System.out.println("onNext");
			}
		});
	}
	@Test
	public void headerTest(){
		final CacheControl.Builder builder = new CacheControl.Builder();
		String ss ;
//		String ss =builder.noCache().build().toString();
		ss = CacheControl.FORCE_CACHE.toString();
//		CacheControl cc = builder.maxAge(30, TimeUnit.SECONDS).build();
//	    ss=	cc.toString();
//
//		okhttp3.Request.Builder builder1 = new okhttp3.Request.Builder().cacheControl(cc);
//		builder1.url("http://www.baidu.com");
//		 okhttp3.Request request =builder1.build();
		System.out.println(ss);
	}
	@Test
	public void array(){
		ProxyHandler.create(retrofit,GitApiInterface.class).getAddrArea(null,0).subscribe(new Subscriber<JsonObject>() {
			@Override
			public void onCompleted() {

			}

			@Override
			public void onError(Throwable e) {

			}

			@Override
			public void onNext(JsonObject jsonObject) {
				System.out.print(jsonObject.toString());
			}
		});
	}
}

package com.ytjojo.practice;

import com.google.gson.JsonObject;
import com.ytjojo.domin.request.LoginRequest;
import com.ytjojo.domin.vo.LoginResponse;
import com.ytjojo.http.GitApiInterface;
import com.ytjojo.http.coverter.GsonConverterFactory;
import com.ytjojo.http.interceptor.ReceivedCookiesInterceptor;
import java.util.ArrayList;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Before;
import org.junit.Test;
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
		HttpLoggingInterceptor i = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
			@Override public void log(String message) {
				System.out.println(message);
			}
		});
		i.setLevel( HttpLoggingInterceptor.Level.BODY);
		OkHttpClient o = new OkHttpClient.Builder().addInterceptor(i).addInterceptor(new ReceivedCookiesInterceptor()).build();

		retrofit = new Retrofit.Builder()
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(GsonConverterFactory.create())
				.client(o)
				.baseUrl("http://ngaribata.ngarihealth.com:8480/ehealth-base-devtest/*.jsonRequest/").build();
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

}

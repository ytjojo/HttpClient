package com.ytjojo.practice;

import com.google.gson.JsonObject;
import com.ytjojo.http.coverter.GsonConverterFactory;
import com.ytjojo.http.interceptor.ReceivedCookiesInterceptor;

import org.junit.Test;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Administrator on 2017/8/19 0019.
 */

public class ArticleTest {
    @Test
    public void test(){

        HttpLoggingInterceptor i = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override public void log(String message) {
                System.out.println(message);
            }
        });
        i.setLevel( HttpLoggingInterceptor.Level.BODY);
        OkHttpClient o = new OkHttpClient.Builder().addInterceptor(i).addInterceptor(new ReceivedCookiesInterceptor()).build();

        Retrofit  retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(o)
                .baseUrl("http://www.ngarihealth.com/").build();
        System.out.println("setUp");
        retrofit.create(ArticleService.class).getList().subscribe(new Subscriber<JsonObject>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(JsonObject jsonObject) {

            }
        });

    }

    public interface ArticleService{

        @GET("api.php/App/getArticlelist_v2?page=0&organid=1&from=app&pubver=2&catid=5")
        Observable<JsonObject>  getList();
    }

}

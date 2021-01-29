package com.ytjojo.practice;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.AbstractClient;
import com.jiulongteng.http.client.AbstractHttpClientFactory;
import com.jiulongteng.http.client.HttpClient;
import com.jiulongteng.http.client.IHttpClientBuilder;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.rx.SimpleObserver;

import org.junit.Test;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.Headers;

public class HttpTest {

    AbstractHttpClientFactory abstractHttpClientFactory ;
    public AbstractHttpClientFactory init(){
        abstractHttpClientFactory = new AbstractHttpClientFactory() {
            @Override
            public File getHttpCacheParent() {
                return null;
            }
        };
        abstractHttpClientFactory.setHttpClientBuilder(new IHttpClientBuilder() {
            @Override
            public AbstractClient createByTag(Object tag) {
                return null;
            }

            @Override
            public AbstractClient createByUrl(String baseUrl) {
                return new HttpClient(baseUrl);
            }

            @Override
            public AbstractClient getDefaultClient() {
                return new HttpClient("http://api.map.baidu.com");
            }
        });
        return abstractHttpClientFactory;
    }

    @Test
    public void test() throws InterruptedException {
        Schedulers.trampoline();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        abstractHttpClientFactory = init();
        abstractHttpClientFactory.getDefaultClient()
               .get("http://api.k780.com/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json")
//                .add("location","嘉兴")
//                .add("output","json")
                .setSync()
                .setBoundaryResultClass(SuccResult.class)
                .asType(new TypeToken<Weather>(){})
                .subscribe(new SimpleObserver<Weather>() {
                    @Override
                    public void onNext(@NonNull Weather jsonElement) {
                        System.out.println(jsonElement.citynm);
                        System.out.println("onNext-------------");

                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println(e.toString() +  " "+ e.getCause().toString());
                        System.out.println("onError-------------");
                        countDownLatch.countDown();
                    }
                });
        countDownLatch.await();;

    }

    @Test
    public void test1() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        abstractHttpClientFactory = init();
        abstractHttpClientFactory.getDefaultClient()
                .get("https://suggest.taobao.com/sug?code=utf-8&q=RO净水器&callback=")
//                .add("location","嘉兴")
//                .add("output","json")
                .setSync()
                .asType(new TypeToken<JsonElement>(){})
                .subscribe(new SimpleObserver<JsonElement>() {
                    @Override
                    public void onNext(@NonNull JsonElement jsonElement) {
                        System.out.println(jsonElement.toString());
                        System.out.println("onNext-------------");

                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        System.out.println(e.toString() +  " "+ e.getCause().toString());
                        System.out.println("onError-------------");
                        countDownLatch.countDown();
                    }
                });
        countDownLatch.await();;

    }

    public static class SuccResult<T> implements IResult<T>{

        public T result;
        public String success;
        public String message;

        @Override
        public int getCode() {
            return 0;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public boolean isSuccessful() {
            return "1".equals(success);
        }

        @Override
        public boolean isInvalidToken() {
            return false;
        }

        @Override
        public void setHeaders(Headers headers) {

        }

        @Override
        public Headers getHeaders() {
            return null;
        }

        @Override
        public T getData() {
            return result;
        }
    }

    public static class Weather{
        public String weaid;
        public String days;
        public String week;
        public String cityno;
        public String citynm;
    }


}

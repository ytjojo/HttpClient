package com.jiulongteng.http.demo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.HttpClient;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.rx.SimpleObserver;

import java.util.List;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HttpClient httpClient = new HttpClient("");
        httpClient.post("")
                .asType(new TypeToken<Integer>(){})

        .toObservable().subscribe(new SimpleObserver<Integer>() {
            @Override
            public void onNext(@NonNull Integer o) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });


    }
}

package com.jiulongteng.http.demo;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.gson.reflect.TypeToken;
import com.jiulongteng.http.client.HttpClient;
import com.jiulongteng.http.entities.IResult;
import com.jiulongteng.http.rx.SimpleObserver;

import io.reactivex.rxjava3.annotations.NonNull;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HttpClient httpClient = new HttpClient("");
//        httpClient.post("")
//                .asType(new TypeToken<Integer>(){})
//               .asResult().toObservable(
//
//
//        });
    }
}

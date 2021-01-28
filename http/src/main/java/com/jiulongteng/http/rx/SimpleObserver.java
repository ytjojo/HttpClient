package com.jiulongteng.http.rx;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

public abstract class SimpleObserver<T> implements Observer<T> {
    public Disposable disposable;

    public SimpleObserver() {
    }

    public void onSubscribe(@NonNull Disposable d) {
        this.disposable = d;
    }

    public void onComplete() {
    }

}

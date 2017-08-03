package com.ytjojo.rx;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class SourceGenerator<T> extends Subscriber<T> implements Observable.OnSubscribe<T> {
    Subscriber<? super T> subscriber;

    @Override
    public void onStart() {

    }


    @Override
    public void onCompleted() {
        if (isAvailable()) {
            subscriber.onCompleted();
        }

    }

    public boolean isAvailable() {
        return (subscriber != null && !subscriber.isUnsubscribed());
    }

    @Override
    public void onError(Throwable e) {
        if (isAvailable()) {
            subscriber.onError(e);
        }
    }

    @Override
    public void onNext(T t) {
        if (isAvailable()) {
            subscriber.onNext(t);
        }
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        this.subscriber = subscriber;
        try {
            subscriber.onStart();
            onStart();
        } catch (Exception ex) {
            onError(ex);
        }
        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                try {
                    onCompleted();
                    SourceGenerator.this.subscriber = null;
                } catch (Exception ex) {
                    onError(ex);
                }

            }
        }));


    }
}

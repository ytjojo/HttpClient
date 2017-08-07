package com.ytjojo.rx;

import android.support.v4.util.Pair;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subjects.BehaviorSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

public class RollingReplaySubject<T> implements Observer<T> {
    private BehaviorSubject<ReplaySubject<T>> subjects;
    private Subject<T,T> replaySubject;



    public RollingReplaySubject() {
        subjects = BehaviorSubject.create();
        subjects.asObservable().subscribe(new Action1<ReplaySubject<T>>() {
            @Override
            public void call(ReplaySubject<T> tReplaySubject) {
                replaySubject = tReplaySubject.toSerialized();
                System.out.println(replaySubject + "初始化");
            }
        });
        subjects.onNext(ReplaySubject.create());
        replaySubject.toSerialized();
    }
    public void clearDuplicate(T t){
            Observable.just(t).lift(new Observable.Operator<T, T>() {
                @Override
                public Subscriber<? super T> call(Subscriber<? super T> subscriber) {
                    Observable.timer(3000,TimeUnit.MILLISECONDS).subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {

                        }
                    });
                    return null;
                }
            });
    }

    @Override
    public void onCompleted() {
        if (replaySubject != null)
            replaySubject.onCompleted();
        replaySubject = null;
    }

    @Override
    public void onError(Throwable e) {
        if (replaySubject != null)
            replaySubject.onError(e);
    }

    @Override
    public void onNext(T t) {
        if (replaySubject != null)
            replaySubject.onNext(t);
    }

    public void clear() {
        if (replaySubject != null)
            replaySubject.onCompleted();
        subjects.onNext(ReplaySubject.create());
    }

    public Subscription subscribe(Subscriber<T> subscriber) {
        return subjects.asObservable().flatMap(new Func1<ReplaySubject<T>, Observable<T>>() {
            @Override
            public Observable<T> call(ReplaySubject<T> tReplaySubject) {
                return replaySubject.asObservable();
            }
        }).subscribe(subscriber);
    }
    public Subscription subscribeTail(Subscriber<T> subscriber) {
        return subjects.asObservable().flatMap(new Func1<ReplaySubject<T>, Observable<T>>() {
            @Override
            public Observable<T> call(ReplaySubject<T> tReplaySubject) {
                return replaySubject.asObservable();
            }
        }).throttleLast(3000, TimeUnit.MILLISECONDS).subscribe(subscriber);
    }

    public <E> Subscription subscribeType(Subscriber<E> subscriber,Class<E> clazz ) {
       return subjects.asObservable().flatMap(new Func1<ReplaySubject<?>, Observable<E>>() {
            @Override
            public Observable<E> call(ReplaySubject<?> tReplaySubject) {
                return replaySubject.asObservable().ofType(clazz);
            }
        }).subscribe(subscriber);
    }
    public <E> Subscription subscribeTypeTail(Subscriber<E> subscriber,Class<E> clazz ) {
        return subjects.asObservable().flatMap(new Func1<ReplaySubject<?>, Observable<E>>() {
            @Override
            public Observable<E> call(ReplaySubject<?> tReplaySubject) {
                return replaySubject.asObservable().ofType(clazz);
            }
        }).lift(new OnResumedLast<>(1500,TimeUnit.MILLISECONDS)).subscribe(subscriber);
    }

    public  <E> void subscribePair(Subscriber<E> subscriber,Class<E> clazz ,Object tag) {
        subjects.asObservable().flatMap(new Func1<ReplaySubject<?>, Observable<?>>() {
            @Override
            public Observable<T> call(ReplaySubject<?> tReplaySubject) {
                return replaySubject.asObservable();
            }
        }).ofType(Pair.class).filter(new Func1<Pair, Boolean>() {
            @Override
            public Boolean call(Pair pair) {
                return clazz.isInstance(pair.second) &&pair.first.equals(tag);
            }
        }).map(new Func1<Pair, E>() {
            @Override
            public E call(Pair pair) {
                return (E) pair.second;
            }
        }).subscribe(subscriber);
    }

}
package com.ytjojo.rx;

import android.support.annotation.NonNull;
import android.util.Log;

import com.trello.rxlifecycle.LifecycleTransformer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 6/11/15.
 */
public class RxBus {
    private static final String TAG = RxBus.class.getSimpleName();
    private static volatile RxBus instance;
    public static boolean DEBUG = false;
//    private final Relay<Object, Object> _bus = PublishRelay.create().toSerialized();
    SerializedSubject allBus;
    // 单例RxBus
    public static RxBus getDefault() {
        RxBus rxBus = instance;
        if (instance == null) {
            synchronized (RxBus.class) {
                rxBus = instance;
                if (instance == null) {
                    rxBus = new RxBus();
                    instance = rxBus;
                }
            }
        }
        return rxBus;
    }

    private RxBus() {
        allBus = new SerializedSubject<>(PublishSubject.create());
    }

    private ConcurrentHashMap<String, Subject> subjectMapper = new ConcurrentHashMap<>();

    public <T> Observable<T> register( @NonNull Class<T> clazz) {
        return toObserverable(clazz);
    }



    private ConcurrentHashMap<Class<?>, CopyOnWriteArrayList<Subscriber<?>>> subscribermaps = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Class<?>, Subscriber<?>> typeOfSubscriber = new ConcurrentHashMap<>();
    public <T> Observable<T> registerObservable(Class<T> clazz){
        Subscriber<T> clazzSubscriber = (Subscriber<T>) typeOfSubscriber.get(clazz);
        if(clazzSubscriber ==null){
            clazzSubscriber = new Subscriber<T>() {
                @Override
                public void onCompleted() {
                    final CopyOnWriteArrayList<Subscriber<?>> subscribers = subscribermaps.get(clazz);
                    Subscriber lastsubscriber = subscribers.get(subscribers.size() - 1);
                    lastsubscriber.onCompleted();
                }

                @Override
                public void onError(Throwable e) {
                    final CopyOnWriteArrayList<Subscriber<?>> subscribers = subscribermaps.get(clazz);
                    Subscriber lastsubscriber = subscribers.get(subscribers.size() - 1);
                    lastsubscriber.onError(e);
                }

                @Override
                public void onNext(T t) {
                    try {
                        final CopyOnWriteArrayList<Subscriber<?>> subscribers = subscribermaps.get(clazz);
                        Subscriber lastsubscriber = subscribers.get(subscribers.size() - 1);
                        lastsubscriber.onNext(t);
                    } catch (Throwable e) {
                        Exceptions.throwOrReport(e, this, t);
                    }
                }

            };
            toObserverable(clazz).subscribe(clazzSubscriber);
            typeOfSubscriber.put(clazz,clazzSubscriber);


        }
        final Subscriber liftSubscriber = clazzSubscriber;
        return Observable.unsafeCreate(new Observable.OnSubscribe<T>() {

            @Override
            public void call(Subscriber<? super T> subscriber) {
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        final CopyOnWriteArrayList<Subscriber<?>> subscribers = subscribermaps.get(clazz);
                        subscribers.remove(subscriber);
                        if(subscribers.isEmpty()){
                            liftSubscriber.unsubscribe();
                            typeOfSubscriber.remove(clazz);
                        }
                    }
                }));
                CopyOnWriteArrayList<Subscriber<?>> subscribers = subscribermaps.get(clazz);
                if(subscribers ==null){
                    subscribers = new CopyOnWriteArrayList<Subscriber<?>>();
                    subscribermaps.put(clazz,subscribers);

                }
                subscribers.add(subscriber);
            }
        });

    }


    public <T> Observable<T> toObserverable (Class<T> eventType) {
        return allBus.ofType(eventType);
    }
    public void unregister(@NonNull String tag, @NonNull Subscription subscription) {
        Subject subjects = subjectMapper.get(tag);
        if (null != subjects) {
            if (!subjects.hasObservers()) {
                subjectMapper.remove(tag);
            }
        }
        if(!subscription.isUnsubscribed()){
            subscription.unsubscribe();
        }

        if (DEBUG) Log.d(TAG, "[unregister]subjectMapper: " + subjectMapper);
    }



    public <T> Observable<T> register(@NonNull String tag, @NonNull Class<T> clazz) {
        Subject subject = subjectMapper.get(tag);
        if (null == subject) {
            subject = new SerializedSubject<>(PublishSubject.create());
            subjectMapper.put(tag, subject);
        }
        if (DEBUG) Log.d(TAG, "[register]subjectMapper: " + subjectMapper);
        return subject.ofType(clazz);
    }
    @SuppressWarnings("unchecked")
    public void post(@NonNull String tag, @NonNull Object content) {
        Subject subject = subjectMapper.get(tag);
        if (subject!=null && subject.hasObservers()) {
            subject.onNext(content);
        }else{
            if (DEBUG) Log.d(TAG, "[send]subjectMapper: failed non regist this type event" );
        }
        if (DEBUG) Log.d(TAG, "[send]subjectMapper: " + subjectMapper);
    }
    public void post(@NonNull Object content) {
//        post(content.getClass().getName(), content);
        allBus.onNext(content);
    }

    /**
     * dosen't designation to use specail thread,It's depending on what the 'send' method use
     *
     * @param lifecycleTransformer rxlifecycle
     * @return
     */
    public <T> Observable<Object> toObserverable(Class<T> clazz,LifecycleTransformer lifecycleTransformer){
        return toObserverable(clazz).compose(lifecycleTransformer);
    }

    /**
     * designation use the MainThread, whatever the 'send' method use
     *
     * @param lifecycleTransformer rxlifecycle
     * @return
     */
    public <T> Observable<Object> toMainThreadObserverable(Class<T> clazz,LifecycleTransformer lifecycleTransformer){
        return toObserverable(clazz).observeOn(AndroidSchedulers.mainThread()).compose(lifecycleTransformer);
    }
}
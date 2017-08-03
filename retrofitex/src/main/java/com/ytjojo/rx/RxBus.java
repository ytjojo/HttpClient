package com.ytjojo.rx;

import android.support.annotation.NonNull;
import android.util.Log;

import com.trello.rxlifecycle.LifecycleTransformer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Author: wangjie
 * Email: tiantian.china.2@gmail.com
 * Date: 6/11/15.
 */
public class RxBus {
    private static final String TAG = RxBus.class.getSimpleName();
    private static volatile RxBus instance;
    public static boolean DEBUG = false;
    HashMap<String,ArrayList<Action1>> classForSubscriptions;
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
        classForSubscriptions=new HashMap<>();
    }

    private ConcurrentHashMap<String, Subject> subjectMapper = new ConcurrentHashMap<>();

    /**
     * 订阅事件源
     *
     * @param mObservable
     * @param mAction1
     * @return
     */
    public RxBus OnEvent(Observable<?> mObservable, Action1<Object> mAction1) {
        mObservable.observeOn(AndroidSchedulers.mainThread()).subscribe(mAction1, (e) -> e.printStackTrace());
        return this;
    }
    public <T> Observable<T> register( @NonNull Class<T> clazz) {
        return toObserverable(clazz);
    }


    public <T> void registerOnEventLast( @NonNull final Class<T>  clazz,Action1<T> onEventAction) {
        ArrayList<Action1> subscriptions=classForSubscriptions.get(clazz.getName());
        if(subscriptions ==null){
            subscriptions = new ArrayList<>();
            classForSubscriptions.put(clazz.getName(),subscriptions);
            toObserverable(clazz).subscribe(new Action1<T>() {
                @Override
                public void call(T t) {
                    ArrayList<Action1> list = classForSubscriptions.get(clazz.getName());
                    if(list !=null &&!list.isEmpty()){
                        list.get(list.size()-1).call(t);
                    }
                }
            });

        }
        subscriptions.add(onEventAction);
    }
    public <T> void unRegisterOnEventLast( @NonNull final Class<T>  clazz,Action1<T> onEventAction) {
        ArrayList<Action1> subscriptions=classForSubscriptions.get(clazz.getName());
        if(subscriptions !=null&&!subscriptions.isEmpty()){
            subscriptions.remove(subscriptions.size() -1);
        }
    }


    public <T> Observable<T> toObserverable (Class<T> eventType) {
        return allBus.ofType(eventType);
    }
    public void unregister(@NonNull String tag, @NonNull Subscription subscription) {
        Subject subjects = subjectMapper.get(tag);
//        if (null != subjects) {
//            if (!subjects.hasObservers()) {
//                subjectMapper.remove(tag);
//            }
//        }
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
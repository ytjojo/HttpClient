package com.ytjojo.rx;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * 用于管理RxBus的事件和Rxjava相关代码的生命周期处理
 * Created by baixiaokang on 16/4/28.
 */
public class RxManage {

    public RxBus mRxBus = RxBus.getDefault();
    private Map<String, Observable<?>> mObservables = new HashMap<>();// 管理观察者
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();// 管理订阅者者


    public <T> void onEvent(String tag, Class<T> clazz,Action1<T> action1) {
        Observable<T> mObservable = mRxBus.register(tag,clazz);
        mObservables.put(tag, mObservable);
        mCompositeSubscription.add(mObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(action1, (e) -> e.printStackTrace()));
    }
    public <T> void onEvent(Class<T> clazz, Action1<T> action1){
        Observable<T> observable = mRxBus.register(clazz);
        mCompositeSubscription.add(observable.observeOn(AndroidSchedulers.mainThread()).subscribe(action1,(e) ->e.printStackTrace()));
    }
    public void add(Subscription m) {
        mCompositeSubscription.add(m);
    }

    public void clear() {
        mCompositeSubscription.unsubscribe();// 取消订阅
        mObservables.clear();
//        for (Map.Entry<String, Observable<?>> entry : mObservables.entrySet()) {
//        }
    }

    public void post(String tag, Object content) {
        mRxBus.post(tag, content);
    }
    public void post(Object object){
        mRxBus.post(object);
    }
}
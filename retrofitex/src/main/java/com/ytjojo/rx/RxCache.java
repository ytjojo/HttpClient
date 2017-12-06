package com.ytjojo.rx;

import android.content.Context;

import com.ytjojo.http.cache.ACache;

import java.io.File;
import java.io.Serializable;
import java.util.LinkedHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;

/**
 * Created by Jam on 16-7-6
 * Description:
 * RxJava + Retrofit 的缓存机制
 */
public class RxCache {

    private final SerializedSubject subject;

    /**
     * @param context
     * @param cacheKey     缓存key
     * @param expireTime   过期时间 0 表示有缓存就读，没有就从网络获取
     * @param fromNetwork  从网络获取的Observable
     * @param forceRefresh 是否强制刷新
     * @param <T>
     * @return
     */
    public static <T> Observable<T> load(final Context context, final String cacheKey, final long expireTime, Observable<T> fromNetwork, boolean forceRefresh) {
        Observable<T> fromCache = Observable.unsafeCreate(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                T cache = (T) CacheManager.readObject(context, cacheKey, expireTime);
                if (cache != null) {
                    subscriber.onNext(cache);
                } else {
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());


        /**
         * 这里的fromNetwork 不需要指定Schedule,在handleRequest中已经变换了
         */
        fromNetwork = fromNetwork.map(new Func1<T, T>() {
            @Override
            public T call(T result) {
                CacheManager.saveObject(context, (Serializable) result, cacheKey);
                return result;
            }
        });
        if (forceRefresh) {
            return fromNetwork;
        } else {
            return Observable.concat(fromCache, fromNetwork).first();
        }

    }


    private LinkedHashMap<String, Object> mClassMap = new LinkedHashMap<>();
    private LinkedHashMap<String, Object> mTagMap = new LinkedHashMap<>();
    ACache mACache;

    public RxCache(File cacheDir, long max_zise) {
        mACache = ACache.get(cacheDir, max_zise, Integer.MAX_VALUE);
        subject = new SerializedSubject<>(PublishSubject.create());
    }

    public <T> Observable<T> get(String tag) {
        Object target = mTagMap.get(tag);
        if (target == null) {
            return Observable.error(new RuntimeException("notfound"));
        } else {
            return Observable.just((T) target);
        }
    }

    public Observable<? extends Serializable> get(Class<? extends Serializable> clazz) {
        Object target = mTagMap.get(clazz.getName());

        if (target == null) {
            return RxCreator.createDefer(new Func0<Observable<Serializable>>() {
                @Override
                public Observable<Serializable> call() {
                    Serializable value = (Serializable) mACache.getAsObject(clazz.getName());
                    if(value == null){
                        return Observable.error(new RuntimeException("notfound"));
                    }
                    return Observable.just(value);
                }
            });
        } else {
            return Observable.just((Serializable) target);
        }
    }

}
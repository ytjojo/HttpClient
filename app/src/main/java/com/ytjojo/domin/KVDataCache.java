package com.ytjojo.domin;

import java.util.List;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/31 0031.
 */
public interface KVDataCache  {
    long EXPIRATION_TIME = 60 * 10 * 1000;//1分钟
    <T> Observable<T>   get(Class<T> clazz);
    void save(Object o);
    void save(String key,Object o);
    <T> void save(String key,List<T> list);
    <T> Observable<T> get(String key,Class<T> clazz);
    void clearAll();
    void delete(String key);
    <T> void delete(Class<T> clazz);
    long generateUpdateTimeMillis(String key);
    long generateUpdateTimeMillis(Class<?> key);
    void setageMillis(String key ,long ageMillis);
    boolean isExpired(String key);
    boolean isExpired(Class<?> clazz);
}

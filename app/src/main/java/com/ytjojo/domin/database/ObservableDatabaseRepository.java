package com.ytjojo.domin.database;

import java.util.List;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/30 0030.
 */
public interface ObservableDatabaseRepository<T> {
    Observable<T>  queryByName(String name);
    Observable<T> queryByDataBaseId(long id);
    Observable<T> queryByBussId(String id);
    Observable<T> queryByBussId(int id);
    Observable<Integer> queryDataCount();
    Observable<List<T>> queryAll();
    Observable<List<T>>  query(String where, String... selectionArg);
    Observable<Boolean>  isContain(T model);
    void save(T model);
    void saveOrUpdate(T model);
    void saveList(List<T> list);
    void delete(T model);
    void clearAll();
    boolean isExpired();
    boolean isExpired(T model);
    void setageMillis(long ageMillis);
}

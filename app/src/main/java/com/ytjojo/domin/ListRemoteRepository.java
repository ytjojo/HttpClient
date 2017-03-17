package com.ytjojo.domin;

import java.util.List;

import rx.Observable;

/**
 * Created by Administrator on 2016/3/29 0029.
 */
public interface ListRemoteRepository<T> {
    Observable<List<T>> getList();
}

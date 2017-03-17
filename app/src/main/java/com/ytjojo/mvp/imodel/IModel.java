package com.ytjojo.mvp.imodel;

import rx.Observable;

public interface IModel<T> {

    Observable<T> getDate();

}
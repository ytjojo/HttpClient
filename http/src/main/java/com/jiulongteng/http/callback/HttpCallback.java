package com.jiulongteng.http.callback;

import com.google.gson.internal.$Gson$Types;
import com.jiulongteng.http.entities.StandardResult;
import com.jiulongteng.http.exception.ExceptionHandle;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class HttpCallback<T> implements Observer<StandardResult<T>> {
    private Type type;

    private Type getSuperclassTypeParameter(Class<?> paramClass) {
        Type type = paramClass.getGenericSuperclass();
        if (!(type instanceof Class)) {
            return $Gson$Types.canonicalize(((ParameterizedType) type).getActualTypeArguments()[0]);

        }
        throw new RuntimeException("Missing type parameter.");
    }

    public Type getResultType() {
        if (this.type == null) {
            this.type = getSuperclassTypeParameter(getClass());

        }
        return this.type;
    }

    public void onComplete() {
    }

    public void onError(Throwable paramThrowable) {
        onResponse(new StandardResult((Throwable) ExceptionHandle.handleException(paramThrowable)));
    }

    public void onNext(StandardResult<T> paramStandardResult) {
        onResponse(paramStandardResult);
    }

    protected abstract void onResponse(StandardResult<T> paramStandardResult);

    public void onSubscribe(Disposable paramDisposable) {
    }
}


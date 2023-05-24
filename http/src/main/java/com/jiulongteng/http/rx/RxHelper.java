package com.jiulongteng.http.rx;


import androidx.lifecycle.GenericLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.jiulongteng.http.exception.AuthException;
import com.jiulongteng.http.exception.ExceptionHandle;
import com.jiulongteng.http.exception.TokenInvalidException;
import com.trello.rxlifecycle4.LifecycleProvider;
import com.trello.rxlifecycle4.LifecycleTransformer;
import com.trello.rxlifecycle4.RxLifecycle;
import com.trello.rxlifecycle4.android.ActivityEvent;
import com.trello.rxlifecycle4.android.FragmentEvent;
import com.trello.rxlifecycle4.components.support.RxFragment;

import java.io.EOFException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableTransformer;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Predicate;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.Subject;
import retrofit2.HttpException;

public class RxHelper {

    public static Function<Observable<? extends Throwable>, Observable<?>> getRetryFunction() {
        return new Function<Observable<? extends Throwable>, Observable<?>>() {
            private long retryDelay = 1000;
            private int retryCount = 0;
            private int maxRetryCount = 3;

            @Override
            public Observable<?> apply(Observable<? extends Throwable> observable) {
                return observable.flatMap(new Function<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> apply(Throwable throwable) {
                        return checkApiError(throwable);
                    }
                });
            }

            private Observable<?> checkApiError(Throwable throwable) {
                retryCount++;
                if (retryCount < maxRetryCount) {
                    if (throwable instanceof ConnectException
                            || throwable instanceof SocketTimeoutException
                            || throwable instanceof TimeoutException
                            || throwable instanceof UnknownHostException
                            || throwable instanceof EOFException) {
                        retryCount = maxRetryCount;
                        return retry(throwable);
                    } else if (throwable instanceof AuthException) {
                        login();
                        return Observable.error(ExceptionHandle.handleException(throwable));
                    } else if (throwable instanceof TokenInvalidException) {
                        login();
                        return Observable.error(ExceptionHandle.handleException(throwable));
                    }
                    if (throwable instanceof HttpException) {
                        HttpException he = (HttpException) throwable;
                        if (he.code() != 401 && he.code() != 403 && he.code() != 409) {
                            return Observable.error(ExceptionHandle.handleException(throwable));
                        } else {
                            return retry(throwable);
                        }
                    }
                    return Observable.error(ExceptionHandle.handleException(throwable));
                } else {
                    if (throwable instanceof HttpException) {
                        HttpException he = (HttpException) throwable;
                        if (he.code() == 401 || he.code() == 403 || he.code() == 409) {
                            login();
                        }
                    }
                    return Observable.error(ExceptionHandle.handleException(throwable));
                }

            }

            /**
             *
             * @param throwable
             * @return
             */
            private Observable<?> retry(Throwable throwable) {
                if (retryCount <= maxRetryCount) {
                    return Observable.timer(retryDelay,
                            TimeUnit.MILLISECONDS).observeOn(Schedulers.io());
                } else {
                    return Observable.error(ExceptionHandle.handleException(throwable));
                }
            }

            private void login() {

            }
        };
    }


    /**
     * @param <T>
     * @return
     */
    public static <T> ObservableTransformer<T, T> applySchedulers() {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(getRetryFunction());

            }
        };
    }

    public static <T> ObservableTransformer<T, T> applySchedulers(final LifecycleTransformer transformer,
                                                                  Class<T> tclass) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread()).retryWhen(getRetryFunction()).compose(transformer);
            }
        };
    }


    public static <T> ObservableTransformer<T, T> applySchedulers(LifecycleTransformer<T> transformer) {
        return new ObservableTransformer<T, T>() {
            @Override
            public Observable<T> apply(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .retryWhen(getRetryFunction())
                        .compose(transformer);
            }
        };
    }


    public static <T> LifecycleTransformer<T> bindUntilOwner(LifecycleOwner owner, Lifecycle.Event event) {
        return bind(takeUntilEvent(ownerToObservable(owner), event));
    }


    public static <T> LifecycleTransformer<T> bindUntilProviderDestroy(LifecycleProvider provider) {
        if (provider instanceof RxFragment) {
            return bind(takeUntilEvent(provider.lifecycle(), FragmentEvent.DESTROY));
        } else {
            return bind(takeUntilEvent(provider.lifecycle(), ActivityEvent.DESTROY));
        }
    }

    public static <T> LifecycleTransformer<T> bindUntilOwnerDestroy(LifecycleOwner owner) {
        return bind(takeUntilEvent(ownerToObservable(owner), Lifecycle.Event.ON_DESTROY));
    }


    public static Observable<Lifecycle.Event> ownerToObservable(LifecycleOwner owner) {
        return Observable.create(new ObservableOnSubscribe<Lifecycle.Event>() {

            @Override
            public void subscribe(ObservableEmitter<Lifecycle.Event> e) throws Exception {
                if (e.isDisposed()) {
                    return;
                }
                if (owner.getLifecycle().getCurrentState().equals(Lifecycle.State.DESTROYED)) {
                    return;
                }
                owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                    @Override
                    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
                        e.onNext(event);
                    }
                });
            }
        });
    }


    public static <T, R> LifecycleTransformer<T> bind(final Observable<R> lifecycle) {
        return RxLifecycle.bind(lifecycle);
    }

    private static <R> Observable<R> takeUntilEvent(final Observable<R> lifecycle, final R event) {
        return lifecycle.filter(new Predicate<R>() {
            @Override
            public boolean test(R lifecycleEvent) throws Exception {
                return lifecycleEvent.equals(event);
            }
        });
    }

    public static <T, R> void observeWhen(Observable<T> observable, Subject<R> subject) {
        observable.zipWith(subject.hide(), new BiFunction<T, R, T>() {

            @Override
            public T apply(T t, R r) throws Exception {
                return t;
            }
        });

    }

}


package com.ytjojo.rx;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.util.Pair;

import com.orhanobut.logger.Logger;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by Administrator on 2016/4/1 0001.
 */
public class RxCreator {


    public static <T> Observable<T> periodically(long INITIAL_DELAY, long POLLING_INTERVAL, Func0<T> func0) {
        return Observable.unsafeCreate(new Observable.OnSubscribe<T>() {
            Subscription subscription;

            @Override
            public void call(Subscriber<? super T> subscriber) {
                final Scheduler.Worker worker = Schedulers.newThread().createWorker();
                subscription = worker.schedulePeriodically(new Action0() {
                    @Override
                    public void call() {
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(func0.call());
                        } else {
                            subscription.unsubscribe();
                        }
                    }
                }, INITIAL_DELAY, POLLING_INTERVAL, TimeUnit.MILLISECONDS);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        try {
                            subscription.unsubscribe();
                        } catch (Exception ex) {
                            // checking for subscribers before emitting values
                            if (!subscriber.isUnsubscribed()) {
                                // (2) - reporting exceptions via onError()
                                subscriber.onError(ex);
                            }
                        }
                    }
                }));
            }
        });
    }



    public static <T, R> Observable<Pair<T, R>> subscribeAllFinish(Observable<T> o1, Observable<R> o2) {
        return Observable.zip(o1, o2, new Func2<T, R, Pair<T, R>>() {
            @Override
            public Pair<T, R> call(T t, R r) {
                Pair<T, R> pair = new Pair<T, R>(t, r);
                return pair;
            }
        });
    }

    public static Observable<Long> sample(int totalSeconds) {
        return Observable.interval(1, TimeUnit.SECONDS)
                .take(totalSeconds + 1).map(new Func1<Long, Long>() {


                    @Override
                    public Long call(Long aLong) {
                        return totalSeconds - aLong;
                    }
                });
    }

    public static void interval(int totalSeconds) {
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .limit(totalSeconds + 1)
                .map(new Func1<Long, Long>() {


                    @Override
                    public Long call(Long aLong) {
                        return totalSeconds - aLong;
                    }
                })
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {

                    }
                })
                .subscribe(new Subscriber<Long>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        Logger.e("aLong" + aLong);
                    }
                });

    }

    public static <T> Observable<T> createDefer(Func0<Observable<T>> func0) {
        return Observable.defer(func0);
    }


    public <T> Observable<T> create(final Callable<T> callable) {
        return Observable.fromCallable(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return callable.call();
            }
        });

    }

    //// 界面按钮需要防止连续点击的情况
    //public void click(View v) {
    //    RxView.clicks(v)
    //            .throttleFirst(680, TimeUnit.MILLISECONDS)
    //            .subscribe(new Action1<Void>() {
    //                @Override
    //                public void call(Void aVoid) {
    //
    //                }
    //            });
    //}

    public <T> void requestWithToken(Observable<T> observable, Observable<String> tokenObservable, String token) {
        Observable.just(null).flatMap(new Func1<Object, Observable<T>>() {
            @Override
            public Observable<T> call(Object o) {
                if (token == null) {
                    return Observable.error(new NullPointerException("Token is null!"));
                } else {
                    return observable;
                }
            }
        }).retryWhen(new Func1<Observable<? extends Throwable>, Observable<?>>() {
            @Override
            public Observable<?> call(Observable<? extends Throwable> observable) {
                return observable.flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if (throwable instanceof IllegalArgumentException || throwable instanceof NullPointerException) {
                            return tokenObservable
                                    .doOnNext(new Action1<String>() {
                                        @Override
                                        public void call(String fakeToken) {

                                        }
                                    });
                        }
                        return Observable.just(throwable);
                    }
                });
            }
        });

    }

    public static <T> List<T> transferAsyncToSync(Observable<T> observable) {
        return observable.toList().toBlocking().single();
    }


    public static Observable<Boolean> verifyLogin(Observable<String> ObservableEmail, Observable<String> ObservablePassword) {
        return Observable.combineLatest(ObservableEmail, ObservablePassword, new Func2<String, String, Boolean>() {
            @Override
            public Boolean call(String email, String password) {
                return isEmailValid(email) && isPasswordValid(password);
            }
        });
    }

    private static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@") && email.contains(".");
    }

    private static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 6;
    }

    /**
     * 推迟执行动作
     *
     * @param delay
     * @param func1
     * @param <R>
     */
    public <R> Observable<R> delayExecute(long delay, Func1<Long, R> func1) {
        return Observable.timer(delay, TimeUnit.SECONDS).map(func1);
    }

    /**
     * 用zip实现推送发送执行结果如下
     */
    public <R> Observable<R> delayDelivery(long delay, Observable<R> observable) {
        return Observable.zip(Observable.timer(delay, TimeUnit.SECONDS), observable, new Func2<Long, R, R>() {

            @Override
            public R call(Long aLong, R r) {
                return r;
            }
        });
    }

    public <T> Observable<T> loadData(Observable<T> memory, Observable<T> disk, Observable<T> net) {
        return Observable.concat(memory, disk, net).first(new Func1<T, Boolean>() {
            @Override
            public Boolean call(T t) {
                return t != null;
            }
        });

    }

    public <T> Observable<T> mergeData(Observable<T> memory, Observable<T> disk, Observable<T> net) {
        return Observable.merge(memory, disk, net).observeOn(AndroidSchedulers.mainThread());

    }

    //public void textwatcher(EditText et, Action1<String> action1) {
    //    RxTextView.textChangeEvents(et)
    //            .debounce(400, TimeUnit.MILLISECONDS)
    //            .observeOn(AndroidSchedulers.mainThread())
    //            .subscribe(new Observer<TextViewTextChangeEvent>() {
    //                @Override
    //                public void onCompleted() {
    //                }
    //
    //                @Override
    //                public void onError(Throwable e) {
    //                }
    //
    //                @Override
    //                public void onNext(TextViewTextChangeEvent onTextChangeEvent) {
    //                    action1.call(onTextChangeEvent.text().toString());
    //                }
    //            });
    //}


    //public static <T> Observable<T> click(View view, Action1 onStart, Observable<T> observable, RxFragment fragment) {
    //    return RxView.clicks(view)
    //            .subscribeOn(AndroidSchedulers.mainThread())
    //            .doOnNext(onStart)
    //            .throttleFirst(400, TimeUnit.MILLISECONDS)
    //            .observeOn(Schedulers.io())
    //            .switchMap(new Func1<Void, Observable<T>>() {
    //                @Override
    //                public Observable<T> call(Void aVoid) {
    //                    return observable;
    //                }
    //            })
    //            .compose(fragment.bindUntilEvent(FragmentEvent.DESTROY_VIEW))
    //            .observeOn(AndroidSchedulers.mainThread());
    //}
    //public static Observable<String> search(EditText et){
    //    return RxTextView.textChangeEvents(et)
    //            .debounce(400, TimeUnit.MILLISECONDS)// default Scheduler is Computation
    //            .map(new Func1<TextViewTextChangeEvent, String>() {
    //
    //                @Override
    //                public String call(TextViewTextChangeEvent textViewTextChangeEvent) {
    //                    return textViewTextChangeEvent.text().toString();
    //                }
    //            })
    //            .filter(changes -> !TextUtils.isEmpty(et.getText().toString()))
    //            .observeOn(AndroidSchedulers.mainThread());
    //}

    public Observable<SensorEvent> naiveObserveSensorChanged(final SensorManager sensorManager, final Sensor sensor, final int samplingPreiodUs) {
        return Observable.unsafeCreate(new Observable.OnSubscribe<SensorEvent>() {
            @Override
            public void call(final Subscriber<? super SensorEvent> subscriber) {
                final SensorEventListener sensorEventListener = new SensorEventListener() {
                    @Override
                    public void onSensorChanged(SensorEvent event) {
                        // (3) - checking for subscribers before emitting values
                        if (!subscriber.isUnsubscribed()) {
                            subscriber.onNext(event);
                        }
                    }

                    @Override
                    public void onAccuracyChanged(Sensor sensor, int accuracy) {
                        // ignored for this example
                    }
                };

                // (1) - unregistering listener when unsubscribed
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        try {
                            sensorManager.unregisterListener(sensorEventListener, sensor);
                        } catch (Exception ex) {
                            // (3) - checking for subscribers before emitting values
                            if (!subscriber.isUnsubscribed()) {
                                // (2) - reporting exceptions via onError()
                                subscriber.onError(ex);
                            }
                        }
                    }
                }));
                sensorManager.registerListener(sensorEventListener, sensor, samplingPreiodUs);
            }
        });
    }

    public static <T> Observable<T> getAsyncObservable(EventSource<T> source) {

        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                return Observable.unsafeCreate(new EventObservable<T>(source));
            }
        });

    }

    public static abstract class EventSource<T> {
        private Subscriber<? super T> mSubscriber;

        public void onDelieveryEvent(T event) {
            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onNext(event);
            }
        }

        public void onSubscriber(Subscriber<? super T> subscriber) {
            this.mSubscriber = subscriber;
        }

        public abstract void onStart();

        public abstract void onStop();

        public void onError(Throwable error) {
            mSubscriber.onError(error);
        }

        public void onStopClear() {
            mSubscriber = null;
        }

    }

    public static class EventObservable<T> implements Observable.OnSubscribe<T> {
        EventSource mSource;

        public EventObservable(EventSource<T> source) {
            this.mSource = source;
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            mSource.onSubscriber(subscriber);
            try {
                mSource.onStart();
            } catch (Exception ex) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(ex);
                }
            }

            subscriber.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    try {
                        mSource.onStop();
                    } catch (Exception ex) {
                        // checking for subscribers before emitting values
                        if (!subscriber.isUnsubscribed()) {
                            // (2) - reporting exceptions via onError()
                            subscriber.onError(ex);
                        }
                    }

                }
            }));

        }


    }


}


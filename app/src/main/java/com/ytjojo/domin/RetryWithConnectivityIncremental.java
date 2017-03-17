package com.ytjojo.domin;

import android.content.Context;
import android.widget.EditText;

import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * RetryWithConnectivityIncremental
 * <p>
 * Created by hanks on 15-11-29.
 */
public class RetryWithConnectivityIncremental implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int startTimeOut;
    private final int maxTimeOut;

    private final TimeUnit timeUnit;

    private int timeOut;

    private Observable<Boolean> isConnected;

    public RetryWithConnectivityIncremental(Context context, int startTimeOut, int maxTimeOut, TimeUnit timeUnit) {
        this.startTimeOut = startTimeOut;
        this.maxTimeOut = maxTimeOut;
        this.timeOut = startTimeOut;
        this.timeUnit = timeUnit;
        isConnected = getConnectedObservable(context);
    }

    private Observable<Boolean> getConnectedObservable(Context context) {
        return BroadcastObservable.fromConnectivityManager(context)
                .distinctUntilChanged()
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean isConnected) {
                        return isConnected;
                    }
                });
    }

    private Observable.Transformer<Boolean, Boolean> attachIncementalTimeOut() {

        return new Observable.Transformer<Boolean, Boolean>() {

            @Override
            public Observable<Boolean> call(Observable<Boolean> observable) {
                return observable.timeout(timeOut, timeUnit).doOnError(new Action1<Throwable>() {

                    @Override
                    public void call(Throwable throwable) {
                        if (throwable instanceof TimeoutException) {
                            timeOut = timeOut > maxTimeOut ? maxTimeOut : timeOut + startTimeOut;
                        }
                    }

                });
            }
        };
    }

    @Override
    public Observable<?> call(final Observable<? extends Throwable> observable) {
        return observable.flatMap(new Func1<Throwable, Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call(Throwable throwable) {
                if (throwable instanceof HttpException) {
                    return isConnected;
                } else {
                    return Observable.error(throwable);
                }
            }
        }).compose(attachIncementalTimeOut());


    }
    private <T> void  request(EditText tv,Class<T> tClass){
        RxTextView.textChanges(tv)
                // 上面的对 tv_result 的操作需要在主线程
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(600, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .filter(new Func1<CharSequence, Boolean>() {
                    @Override public Boolean call(CharSequence charSequence) {
                        // 清空搜索出来的结构
                        tv.setText("");
                        //当 EditText 中文字大于0的时候
                        return charSequence.length() > 0;
                    }
                })
                .switchMap(new Func1<CharSequence, Observable<T>>() {
                    @Override public Observable<T> call(CharSequence charSequence) {
                        // 搜索
                        return null;
                    }
                })
//                .retryWhen(new RetryWithConnectivityIncremental(MainActivity.this, 5, 15, TimeUnit.MILLISECONDS))
                // 网络操作在io线程
                .subscribeOn(Schedulers.io())
                //将 body 转换成 ArrayList<ArrayList<String>>
                .map(new Func1<T, ArrayList<ArrayList<String>>>() {
                    @Override public ArrayList<ArrayList<String>> call(T data) {
                        return new ArrayList<ArrayList<String>>();
                    }
                })
                // 将 ArrayList<ArrayList<String>> 中每一项提取出来 ArrayList<String>
                .flatMap(new Func1<ArrayList<ArrayList<String>>, Observable<ArrayList<String>>>() {
                    @Override public Observable<ArrayList<String>> call(ArrayList<ArrayList<String>> arrayLists) {
                        return Observable.from(arrayLists);
                    }
                })
                .filter(new Func1<ArrayList<String>, Boolean>() {
                    @Override public Boolean call(ArrayList<String> strings) {
                        return strings.size() >= 2;
                    }
                })
                .map(new Func1<ArrayList<String>, String>() {
                    @Override public String call(ArrayList<String> strings) {
                        return "[商品名称:" + strings.get(0) + ", ID:" + strings.get(1) + "]\n";
                    }
                })
                // 发生错误后不要调用 onError，而是转到 onErrorResumeNext
                /*.onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override public Observable<? extends String> call(Throwable throwable) {
                        return Observable.just("error result");
                    }
                })*/

                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override public void call(String charSequence) {
//                        showpop(charSequence);
                    }
                });
    }
}
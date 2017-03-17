package com.ytjojo.rx;

import android.util.Log;

import com.ytjojo.http.exception.TokenInvalidException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by jess on 9/2/16 14:32
 * Contact with jess.yan.effort@gmail.com
 */
public class RetryWithDelay implements
        Func1<Observable<? extends Throwable>, Observable<?>> {
    public final String TAG = this.getClass().getSimpleName();
    private  int maxRetries=3;
    private  int retryDelaySecond=5000;
    private int retryCount ;
    Func1<Throwable,Boolean> mBooleanThrowableFunc1;
    public RetryWithDelay(){

    }
    public RetryWithDelay(int maxRetries, int retryDelaySecond) {
        this.maxRetries = maxRetries;
        this.retryDelaySecond = retryDelaySecond;
    }
    public RetryWithDelay(int maxRetries, int retryDelaySecond, Func1<Throwable,Boolean> func1) {
        this.maxRetries = maxRetries;
        this.retryDelaySecond = retryDelaySecond;
        this.mBooleanThrowableFunc1 = func1;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts
                .flatMap(new Func1<Throwable, Observable<?>>() {
                    @Override
                    public Observable<?> call(Throwable throwable) {
                        if(mBooleanThrowableFunc1 !=null){
                            if(mBooleanThrowableFunc1.call(throwable)){
                                if (++retryCount <= maxRetries) {
                                    // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                                    Log.d(TAG, "get error, it will try after " + retryDelaySecond
                                            + " second, retry count " + retryCount);
                                    return Observable.timer(retryDelaySecond*retryCount,
                                            TimeUnit.SECONDS);
                                }
                            }
                        }else{
                            if (++retryCount <= maxRetries) {
                                // When this Observable calls onNext, the original Observable will be retried (i.e. re-subscribed).
                                Log.d(TAG, "get error, it will try after " + retryDelaySecond
                                        + " second, retry count " + retryCount);
                                return Observable.timer(retryDelaySecond,
                                        TimeUnit.SECONDS);
                            }
                        }

                        // Max retries hit. Just pass the error along.
                        return Observable.error(throwable);
                    }
                });
    }

    public static Func1<Throwable,Boolean> getFiter(){
        return new Func1<Throwable,Boolean>(){

            @Override
            public Boolean call(Throwable e) {
                Throwable throwable = e;
                //获取最根源的异常
                while(throwable.getCause() != null){
                    e = throwable;
                    throwable = throwable.getCause();
                }
                if(e instanceof ConnectException
                        ||e instanceof SocketTimeoutException
                        || e instanceof TimeoutException
                        ||e instanceof HttpException
                        || e instanceof NullPointerException
                        || e instanceof TokenInvalidException){

                    return true;
                }
                return false;
            }
        };
    }
}

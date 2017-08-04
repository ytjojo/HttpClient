package com.ytjojo.http.subscriber;

import android.content.Context;
import android.widget.Toast;
import com.orhanobut.logger.Logger;
import com.ytjojo.http.exception.APIException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import rx.Subscriber;

public abstract class BaseSubscriber<T> extends Subscriber<T> {

    protected Context context;

    public BaseSubscriber() {

    }

    public BaseSubscriber(Context applicationContext) {
        this.context = applicationContext.getApplicationContext();
    }

    @Override
    public void onError(Throwable throwable) {
        Throwable e = throwable;
        while (throwable.getCause() != null) {
            e = throwable;
            throwable = throwable.getCause();
        }
        if (e instanceof ConnectException || e instanceof SocketTimeoutException || e instanceof TimeoutException||e instanceof UnknownHostException) {
            onNetworkException(e);
        } else if (e instanceof APIException) {
            onAPIException((APIException) e);
        } else {
            onUnknownException(e);
        }
    }

    @Override
    public abstract void onNext(T t);


    @Override
    public void onCompleted() {

    }

    public void onAPIException(APIException e) {
        e.printStackTrace();
        if (context != null) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onNetworkException(Throwable e) {
        if (context != null) {
            Toast.makeText(context, "网络较慢，请稍候...", Toast.LENGTH_SHORT).show();
        }
    }

    public void onUnknownException(Throwable e) {
        e.printStackTrace();
        Logger.e(e.getMessage());
    }

}
package com.jiulongteng.http.factory;

import androidx.annotation.Nullable;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;

public final class ObserveOnMainCallAdapterFactory extends CallAdapter.Factory {
    final Scheduler scheduler;

    public ObserveOnMainCallAdapterFactory(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public @Nullable CallAdapter<?, ?> get(
            Type returnType, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(returnType) != Observable.class) {
            return null; // Ignore non-Observable types.
        }

        // Look up the next call adapter which would otherwise be used if this one was not present.
        //noinspection unchecked returnType checked above to be Observable.
        final CallAdapter<Object, Observable<?>> delegate =
                (CallAdapter<Object, Observable<?>>)
                        retrofit.nextCallAdapter(this, returnType, annotations);

        return new CallAdapter<Object, Object>() {
            @Override
            public Object adapt(Call<Object> call) {
                // Delegate to get the normal Observable...
                Observable<?> o = delegate.adapt(call);
                // ...and change it to send notifications to the observer on the specified scheduler.
                return o.observeOn(scheduler);
            }

            @Override
            public Type responseType() {
                return delegate.responseType();
            }
        };
    }
}

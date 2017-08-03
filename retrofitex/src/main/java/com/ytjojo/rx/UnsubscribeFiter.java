package com.ytjojo.rx;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Func1;
import rx.plugins.RxJavaHooks;

/**
 * Created by Administrator on 2016/11/18 0018.
 */
public class UnsubscribeFiter<T> implements Observable.Operator<T,T>{
    final Func1<? super T, Boolean> predicate;
    public UnsubscribeFiter( Func1<? super T, Boolean> predicate){
        this.predicate = predicate;
    }
    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        FilterSubscriber filterSubscriber = new FilterSubscriber(child,predicate);
        child.add(filterSubscriber);
        return filterSubscriber;
    }
    static final class FilterSubscriber<T> extends Subscriber<T> {

        final Subscriber<? super T> actual;

        final Func1<? super T, Boolean> predicate;

        boolean done;

        public FilterSubscriber(Subscriber<? super T> actual, Func1<? super T, Boolean> predicate) {
            this.actual = actual;
            this.predicate = predicate;
        }

        @Override
        public void onNext(T t) {
            boolean result;
            try {
                result = predicate.call(t);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                unsubscribe();
                onError(OnErrorThrowable.addValueAsLastCause(ex, t));
                return;
            }

            if (result) {
                actual.onNext(t);
            } else {
                if(!actual.isUnsubscribed()){
                    synchronized (UnsubscribeFiter.class){
                       if(!actual.isUnsubscribed()){
                           actual.unsubscribe();
                       }

                    }
                }

            }
        }

        @Override
        public void onError(Throwable e) {
            if (done) {
                RxJavaHooks.onError(e);
                return;
            }
            done = true;

            actual.onError(e);
        }


        @Override
        public void onCompleted() {
            if (done) {
                return;
            }
            actual.onCompleted();
        }
        @Override
        public void setProducer(Producer p) {
            super.setProducer(p);
            actual.setProducer(p);
        }
    }
}

package com.ytjojo.rx;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.internal.producers.ProducerArbiter;
import rx.plugins.RxJavaHooks;
import rx.subscriptions.SerialSubscription;

/**
 * Created by Administrator on 2017/9/7 0007.
 */

public class OperatorOnErrorFlatmap <R,T> implements Observable.Operator<R, T> {
    final Func1<? super Throwable, ? extends Observable<? extends R>> resumeFunction;
    final Action1<T> onNext;
    public OperatorOnErrorFlatmap(Func1<? super Throwable, ? extends Observable<? extends R>> resumeFunction,Action1<T> onNext){
        this.onNext = onNext;
        this.resumeFunction = resumeFunction;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super R> child) {
        final ProducerArbiter pa = new ProducerArbiter();

        final SerialSubscription serial = new SerialSubscription();

        Subscriber<T> parent = new Subscriber<T>() {

            private boolean done;

            long produced;

            @Override
            public void onCompleted() {
                if (done) {
                    return;
                }
                done = true;
                child.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                if (done) {
                    Exceptions.throwIfFatal(e);
                    RxJavaHooks.onError(e);
                    return;
                }
                done = true;
                try {
                    unsubscribe();

                    Subscriber<R> next = new Subscriber<R>() {
                        @Override
                        public void onNext(R t) {
                            child.onNext(t);
                        }
                        @Override
                        public void onError(Throwable e) {
                            child.onError(e);
                        }
                        @Override
                        public void onCompleted() {
                            child.onCompleted();
                        }
                        @Override
                        public void setProducer(Producer producer) {
                            pa.setProducer(producer);
                        }
                    };
                    serial.set(next);

                    long p = produced;
                    if (p != 0L) {
                        pa.produced(p);
                    }

                    Observable<? extends R> resume = resumeFunction.call(e);

                    resume.unsafeSubscribe(next);
                } catch (Throwable e2) {
                    Exceptions.throwOrReport(e2, child);
                }
            }

            @Override
            public void onNext(T t) {
                if (done) {
                    return;
                }
                produced++;
                onNext.call(t);
            }

            @Override
            public void setProducer(final Producer producer) {
                pa.setProducer(producer);
            }

        };
        serial.set(parent);

        child.add(serial);
        child.setProducer(pa);

        return parent;
    }

}

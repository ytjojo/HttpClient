package com.ytjojo.rx;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.internal.schedulers.ImmediateScheduler;
import rx.internal.schedulers.TrampolineScheduler;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/11/10 0010.
 */
public class DoAsync <T> implements Observable.Operator<T,T> {
    final Action1<T> action;
    final Scheduler scheduler;
    public DoAsync(Scheduler scheduler,Action1 action){
        this.scheduler =scheduler;
        this.action = action;
    }
    public DoAsync(Action1 action){
        this.scheduler = Schedulers.io();
        this.action = action;
    }
    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {

        final Scheduler.Worker worker = scheduler.createWorker();
        DoAsyncSubscriber<T> subscriber = new DoAsyncSubscriber<>(scheduler,action,worker,child);
        child.add(subscriber);
        child.add(worker);
        return  subscriber;
    }
    static final class DoAsyncSubscriber<T> extends Subscriber<T> implements Action0 {
        T value;
        final Scheduler scheduler;
        final Scheduler.Worker worker;
        final Action1<T> action;
        final Subscriber<? super T> child;
        public DoAsyncSubscriber(Scheduler scheduler,Action1<T> action,Scheduler.Worker worker,Subscriber<? super T> child){
            this.action = action;
            this.scheduler = scheduler;
            this.worker = worker;
            this.child = child;
        }
        @Override
        public void call() {
            action.call(value);
        }

        @Override
        public void onCompleted() {
            child.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onNext(T t) {
            value = t;
            boolean called = false;
            if (scheduler instanceof ImmediateScheduler || scheduler instanceof TrampolineScheduler) {
                //  execute directly
                call();
                called = true;
            }
            child.onNext(t);
            if(!called){
               worker.schedule(this);
            }

        }
    }
}

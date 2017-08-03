package com.ytjojo.rx;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Action0;
import rx.observers.SerializedSubscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Administrator on 2016/11/9 0009.
 */
public class OnResumedLast<T> implements Observable.Operator<T,T> {
    final long time;
    final TimeUnit unit;
    final Scheduler scheduler;
    public OnResumedLast(long time, TimeUnit unit, Scheduler scheduler){
        this.time = time;
        this.unit = unit;
        this.scheduler = scheduler;
    }
    public OnResumedLast(long time, TimeUnit unit){
        this.time = time;
        this.unit = unit;
        this.scheduler = Schedulers.computation();
    }
    public OnResumedLast(){
        this.time = 30;
        this.unit = TimeUnit.MILLISECONDS;
        this.scheduler = Schedulers.computation();
    }

    @Override
    public Subscriber<? super T> call(Subscriber<? super T> child) {
        final SerializedSubscriber<T> s = new SerializedSubscriber<T>(child);
        final Scheduler.Worker worker = scheduler.createWorker();
        child.add(worker);
        SamplerSubscriber<T> sampler = new SamplerSubscriber<>(worker,s,time,unit);
        child.add(sampler);
        return sampler;
    }

    /**
     * The source subscriber and sampler.
     */
    static final class SamplerSubscriber<T> extends Subscriber<T> implements Action0 {
        private final Subscriber<? super T> subscriber;
        /** Indicates that no value is available. */
        private static final Object EMPTY_TOKEN = new Object();
        /** The shared value between the observer and the timed action. */
        final AtomicReference<Object> value = new AtomicReference<Object>(EMPTY_TOKEN);
        final Scheduler.Worker worker;
        final AtomicBoolean isBeginSchedule= new AtomicBoolean(false);
        final long time;
        final TimeUnit unit;
        volatile boolean done;
        public SamplerSubscriber(Scheduler.Worker worker,Subscriber<? super T> subscriber,long time, TimeUnit unit) {
            this.worker = worker;
            this.subscriber = subscriber;
            this.time = time;
            this.unit = unit;
        }

        @Override
        public void onStart() {
            request(Long.MAX_VALUE);
        }

        @Override
        public void onNext(T t) {
            value.set(t);
            if(isBeginSchedule.compareAndSet(false,true)){
                worker.schedule(this,time,unit);
            }
        }

        @Override
        public void onError(Throwable e) {
            subscriber.onError(e);
            unsubscribe();
        }

        @Override
        public void onCompleted() {
            emitIfNonEmpty();
            subscriber.onCompleted();
            unsubscribe();
            done = true;
        }

        @Override
        public void call() {
            if(done){
                return;
            }
            isBeginSchedule.set(false);
            emitIfNonEmpty();
        }

        private void emitIfNonEmpty() {
            Object localValue = value.getAndSet(EMPTY_TOKEN);
            if (localValue != EMPTY_TOKEN) {
                try {
                    @SuppressWarnings("unchecked")
                    T v = (T)localValue;
                    subscriber.onNext(v);
                } catch (Throwable e) {
                    Exceptions.throwOrReport(e, this);
                }
            }
        }
    }
}

package com.ytjojo.domin.interactor;

import com.ytjojo.domin.ListRemoteRepository;
import com.ytjojo.domin.database.ObservableDatabaseRepository;

import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by Administrator on 2016/4/1 0001.
 */
public class UserCaseWithCache<T> {


    private final ThreadExecutor threadExecutor;
    private final PostExecutionThread postExecutionThread;
    private final ListRemoteRepository<T> listRemoteRepository;
    private final ObservableDatabaseRepository<T> observableDatabaseRepository;

    private Subscription subscription = Subscriptions.empty();

    public UserCaseWithCache(ThreadExecutor threadExecutor,
                             PostExecutionThread postExecutionThread, ListRemoteRepository<T> listRemoteRepository, ObservableDatabaseRepository<T> observableDatabaseRepository) {
        this.threadExecutor = threadExecutor;
        this.postExecutionThread = postExecutionThread;
        this.listRemoteRepository = listRemoteRepository;
        this.observableDatabaseRepository = observableDatabaseRepository;
    }


    public void execute() {
        Observable.concat(observableDatabaseRepository.queryAll(), listRemoteRepository.getList())
                .first()
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<T>>() {
                    @Override
                    public void call(List<T> ts) {

                    }
                });
    }

    private void loadFromNet() {
        listRemoteRepository.getList()
                .doOnNext(new Action1<List<T>>() {
                    @Override
                    public void call(List<T> ts) {
                        observableDatabaseRepository.clearAll();
                        observableDatabaseRepository.saveList(ts);
                    }
                });
    }

    private void laodFremDb() {
        observableDatabaseRepository.queryAll()
                .filter(new Func1<List<T>, Boolean>() {
                    @Override
                    public Boolean call(List<T> ts) {
                        if (ts == null || ts.isEmpty()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                });
    }

    /**
     * Unsubscribes from current {@link Subscription}.
     */
    public void unsubscribe() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

}

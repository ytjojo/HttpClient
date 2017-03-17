package com.ytjojo.mvp.presenter;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;
import com.ytjojo.mvp.mvpview.MvpView;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Base class that implements the Presenter interface and provides a base implementation for
 * attachView() and detachView(). It also handles keeping a reference to the mvpView that
 * can be accessed from the children classes by calling getMvpView().
 */
public class BasePresenter<T extends MvpView> implements Presenter<T> {

    private T mMvpView;
    private CompositeSubscription mCompositeSubscription;

    public final boolean isViewAttached() {
        return mMvpView != null;
    }

    public T getMvpView() {
        return mMvpView;
    }

    public void checkViewAttached() {
        if (!isViewAttached()) throw new MvpViewNotAttachedException();
    }

    @Override
    public void onViewAttached(T view) {
        mMvpView = view;
        checkViewAttached();
        Observable<ActivityEvent> activityLifecycle = mMvpView.lifecycleActivty();
        if(activityLifecycle != null){
            activityLifecycle.subscribe(new Action1<ActivityEvent>() {
                @Override
                public void call(ActivityEvent event) {
                    activityLifecycle(event);
                }
            });
        }
        Observable<FragmentEvent> fragmentLifecycle = mMvpView.lifecycleFragment();
        if(fragmentLifecycle != null){
            fragmentLifecycle.subscribe(new Action1<FragmentEvent>() {
                @Override
                public void call(FragmentEvent fragmentEvent) {
                    fragmentLifecycle(fragmentEvent);
                }
            });
        }

    }

    @Override
    public void onViewDetached() {
        mMvpView = null;
    }

    @Override
    public void onViewLayouted() {

    }

    @Override
    public void activityLifecycle(ActivityEvent event) {
        switch (event){
            case DESTROY:
                onUnsubscribe();
                break;
        }
    }

    @Override
    public void fragmentLifecycle(FragmentEvent event) {
        switch (event){
            case DESTROY:
                onUnsubscribe();
                break;
        }
    }


    public static class MvpViewNotAttachedException extends RuntimeException {
        public MvpViewNotAttachedException() {
            super("Please call Presenter.attachView(MvpView) before" +
                    " requesting body to the Presenter");
        }
    }

    //RXjava取消注册，以避免内存泄露
    public void onUnsubscribe() {
        if (mCompositeSubscription != null && mCompositeSubscription.hasSubscriptions()) {
            mCompositeSubscription.unsubscribe();
        }
    }


    public void addSubscription(Observable observable, Subscriber subscriber) {
        if (mCompositeSubscription == null) {
            mCompositeSubscription = new CompositeSubscription();
        }
        mCompositeSubscription.add(
                observable.subscribe(subscriber));
    }
}


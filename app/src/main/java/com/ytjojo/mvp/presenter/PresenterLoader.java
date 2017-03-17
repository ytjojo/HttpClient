package com.ytjojo.mvp.presenter;

import android.content.Context;
import android.support.v4.content.Loader;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

public class PresenterLoader<T extends Presenter> extends Loader<T> {

    private final PresenterFactory<T> factory;
    private T presenter;

    /**
     * Stores away the application context associated with context.
     * Since Loaders can be used across multiple activities it's dangerous to
     * store the context directly; always use {@link #getContext()} to retrieve
     * the Loader's Context, don't use the constructor argument directly.
     * The Context returned by {@link #getContext} is safe to use across
     * Activity instances.
     *
     * @param context used to retrieve the application context.
     */
    public PresenterLoader(Context context,PresenterFactory<T> factory) {
        super(context);
        this.factory = factory;
    }

    // 省略构造方法

    @Override
    protected void onStartLoading() {

        // 如果已经有Presenter实例那就直接返回
        if (presenter != null) {
            deliverResult(presenter);
            return;
            }

        // 如果没有
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        // 通过工厂来实例化Presenter
        presenter = factory.create();

        // 返回Presenter
        deliverResult(presenter);
    }

    @Override
    protected void onReset() {
        presenter.fragmentLifecycle(FragmentEvent.DESTROY);
        presenter.activityLifecycle(ActivityEvent.DESTROY);
        presenter = null;
    }
}
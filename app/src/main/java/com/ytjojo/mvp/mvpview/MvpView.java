package com.ytjojo.mvp.mvpview;

import android.content.Context;
import android.view.View;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

import rx.Observable;

public interface MvpView {

    Context getContext();
    void showLoading(String msg);
    void hideLoading();
    void showEmptyView(String msg, String buttonText, View.OnClickListener l);
    void showError(String msg,View.OnClickListener l);
    void showContent();
    void onClick(View v);
    Observable<ActivityEvent> lifecycleActivty();
    Observable<FragmentEvent> lifecycleFragment();
}

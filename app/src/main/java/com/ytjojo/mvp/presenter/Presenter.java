package com.ytjojo.mvp.presenter;

import com.trello.rxlifecycle.android.ActivityEvent;
import com.trello.rxlifecycle.android.FragmentEvent;

public interface Presenter<V> {
    void onViewAttached(V view);

    void onViewDetached();
    /**
     * Method that control the lifecycle of the view. It should be called in the view's
     * view.post(runable)or onGlobleLayoutListener
     */
    void onViewLayouted();

    void activityLifecycle(ActivityEvent event);

    void fragmentLifecycle(FragmentEvent event);

//    /**
//     * Method that control the lifecycle of the view. It should be called in the view's
//     * (Activity or Fragment) onResume() method.
//     */
//    void onResume();
//
//    /**
//     * Method that controls the lifecycle of the view. It should be called in the view's
//     * (Activity or Fragment) onPause() method.
//     */
//    void onPause();
//
//    /**
//     * Method that controls the lifecycle of the view. It should be called in the view's
//     * (Activity or Fragment) onStop() method.
//     */
//    void onStart();
//    /**
//     * Method that controls the lifecycle of the view. It should be called in the view's
//     * (Activity or Fragment) onStop() method.
//     */
//    void onStop();
//
//    /**
//     * Method that control the lifecycle of the view. It should be called in the view's
//     * (Activity or Fragment) onDestroy() method.
//     */
//    void onDestroyed();
}
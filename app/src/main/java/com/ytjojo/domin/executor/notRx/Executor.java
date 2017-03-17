package com.ytjojo.domin.executor.notRx;


/**
 * This executor is responsible for running interactors on background threads.
 * <p/>
 * Created by dmilicic on 7/29/15.
 */
public interface Executor {

    /**
     * This method should call the interactor's run method and thus start the interactor. This should be called
     * on a background thread as interactors might do lengthy operations.
     *
     * @param interactor The interactor to run.
     */
    void execute(final AbstractInteractor interactor);
}

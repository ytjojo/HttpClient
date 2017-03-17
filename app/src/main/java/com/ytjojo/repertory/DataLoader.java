package com.ytjojo.repertory;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.os.CancellationSignal;
import android.support.v4.os.OperationCanceledException;

import java.util.List;

/**
 * Created by LIUYONGKUI on 2016-05-30.
 */
public class DataLoader <T> extends AsyncTaskLoader<List<T>> {

    CancellationSignal mCancellationSignal;

    private List<T> mData;

    public DataLoader(Context context) {
        super(context);
    }

    @Override
    public List<T> loadInBackground() {
        synchronized (this) {
            if (isLoadInBackgroundCanceled()) {
                throw new OperationCanceledException();
            }
            mCancellationSignal = new CancellationSignal();
        }
        if (mData == null) {
            if(dataFetcher !=null){
                mData = dataFetcher.fetchDataImmidiate();
            }
        }

        try {

        } finally {
            synchronized (this) {
                mCancellationSignal = null;
            }
        }
        return mData;
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        synchronized (this) {
            if (mCancellationSignal != null) {
                mCancellationSignal.cancel();
            }
        }
    }

   /**
     * Called when there is new body to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override
    public void deliverResult(List<T> data) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (data != null) {
                onReleaseResources(data);
            }
        }
        List<T> olds = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(data);
        }

        if (olds != null) {
            onReleaseResources(olds);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {


        if (mData != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(mData);
        }
        if (takeContentChanged() || mData == null) {
            // If the body has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();

    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(List<T> data) {
        super.onCanceled(data);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(data);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (mData != null) {
            onReleaseResources(mData);
            mData = null;
        }

    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded body set.
     */
    protected void onReleaseResources(List<T> data) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
        mData = null;
    }
    DataFetcher<List<T>> dataFetcher;
    public void setDataFetcher( DataFetcher<List<T>> dataFetcher){
        this.dataFetcher = dataFetcher;
    }

    public interface DataFetcher<M>{

        M fetchDataImmidiate();

    }

}
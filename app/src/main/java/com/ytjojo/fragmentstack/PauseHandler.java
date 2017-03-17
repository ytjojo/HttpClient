package com.ytjojo.fragmentstack;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Message Handler class that supports buffering up of messages when the
 * activity is paused i.e. in the background.
 */
public abstract class PauseHandler {
    private final static String KEY_COMMIT_ACTION_TAGS = "KEY_COMMIT_ACTION_TAGS_PauseHandler";
    /**
     * Message Queue Buffer
     */
    final Vector<CommitAction> mRunnableQueueBuffer = new Vector<CommitAction>();
    final public Activity mActivity;

    public PauseHandler(Activity activity, Bundle savedInstanceState) {
        this.mActivity = activity;
        final ArrayList<String> mActionTags;
        mActionTags = new ArrayList<>();
        if (savedInstanceState != null) {
            mActionTags.addAll(savedInstanceState.getStringArrayList("KEY_COMMIT_ACTION_TAGS"));
            for (String tag : mActionTags) {
                final Runnable runnable = generateCommitActionByTag(tag);
                if (runnable != null)
                    mActivity.runOnUiThread(runnable);
            }
        }

    }

    public void onSaveInstanceState(Bundle outState) {
        if (mRunnableQueueBuffer.size() > 0) {
            final ArrayList<String> actionTags = new ArrayList<>();
            for (CommitAction action : mRunnableQueueBuffer) {
                actionTags.add(action.tag);
            }
            outState.putStringArrayList(KEY_COMMIT_ACTION_TAGS, actionTags);
        }

    }

    /**
     * Flag indicating the pause state
     */
    private boolean paused;

    /**
     * Resume the handler
     */
    final public void resume() {
        paused = false;
        processBufferRunnable();
    }

    /**
     * Pause the handler
     */
    final public void pause() {
        paused = true;
    }


    public abstract boolean storeRunnable(CommitAction runnable);

    public abstract Runnable generateCommitActionByTag(String tag);


    private void processBufferRunnable() {
        while (mRunnableQueueBuffer.size() > 0) {
            final Runnable runnable = mRunnableQueueBuffer.elementAt(0);
            mRunnableQueueBuffer.removeElementAt(0);
            mActivity.runOnUiThread(runnable);
        }
    }

    final public void handleMessage(CommitAction runnable) {
        if (paused) {
            if (storeRunnable(runnable)) {
                mRunnableQueueBuffer.add(runnable);
            }
        } else {
            runnable.run();
        }
    }

    public static abstract class CommitAction implements Runnable {
        public String tag;
        public boolean isNeedStore;

    }
}
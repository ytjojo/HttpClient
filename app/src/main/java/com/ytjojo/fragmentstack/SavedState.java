package com.ytjojo.fragmentstack;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * This is the persistent state that is saved by ViewPager.  Only needed
 * if you are creating a sublass of ViewPager that must save its own
 * state, in which case it should implement a subclass of this which
 * contains that state.
 */
public class SavedState implements Parcelable {
    ArrayList<ActionState> mActionStates;
    ArrayList<String> mFragmentBackTags;

    public SavedState(){
        mActionStates = new ArrayList<>();
        mFragmentBackTags = new ArrayList<>();
    }
    protected SavedState(Parcel in) {
        mActionStates = in.createTypedArrayList(ActionState.CREATOR);
        mFragmentBackTags = in.createStringArrayList();
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        @Override
        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mActionStates);
        dest.writeStringList(mFragmentBackTags);
    }
}
package com.ytjojo.fragmentstack;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/8/22 0022.
 */
public class ActionState implements Parcelable,Serializable {
    public static final int ActionType_POP =  1;
//    public static final int ActionType_POP_TOROOT = 2;
    public static final int ActionType_POP_TO =3;
    public static final int ActionType_REMOVE =4;
    public static final int ActionType_START = 5;
    public static final int ActionType_START_FOR_RESULT = 6;
//    public static final int ActionType_START_MULT = 7;
//    public static final int ActionType_START_WITH_POP =8;
    public static final int ActionType_REPLACE_ROOT = 9;
    int mActionType=ActionType_POP;
    String mPopToTag;
    boolean includeSelf;
    ArrayList<String> mToRemoveFragmentTags;
    ArrayList<String> mToStartFragments;
    ArrayList<Bundle> mArguments;

    public ActionState() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mActionType);
        dest.writeString(this.mPopToTag);
        dest.writeByte((byte)(includeSelf ?1:0));
        dest.writeStringList(this.mToRemoveFragmentTags);
        dest.writeStringList(this.mToStartFragments);
        dest.writeTypedList(mArguments);
    }

    protected ActionState(Parcel in) {
        this.mActionType = in.readInt();
        this.mPopToTag = in.readString();
        this.includeSelf = in.readByte() ==1;
        this.mToRemoveFragmentTags = in.createStringArrayList();
        this.mToStartFragments = in.createStringArrayList();
        this.mArguments = in.createTypedArrayList(Bundle.CREATOR);
    }

    public static final Creator<ActionState> CREATOR = new Creator<ActionState>() {
        public ActionState createFromParcel(Parcel source) {
            return new ActionState(source);
        }

        public ActionState[] newArray(int size) {
            return new ActionState[size];
        }
    };
}

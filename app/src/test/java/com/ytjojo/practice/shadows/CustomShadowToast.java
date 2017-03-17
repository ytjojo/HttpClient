package com.ytjojo.practice.shadows;

import android.content.Context;
import android.widget.Toast;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Toast.class)
public class CustomShadowToast {

    private static boolean mIsShown;
    @RealObject
    Toast toast;
    public void __constructor__(Context context) {
    }

    @Implementation
    public void show() {
        mIsShown = true;
        Shadows.shadowOf(RuntimeEnvironment.application).getShownToasts().add(toast);
    }

    public static boolean isToastShowInvoked() {
        return mIsShown;
    }
}
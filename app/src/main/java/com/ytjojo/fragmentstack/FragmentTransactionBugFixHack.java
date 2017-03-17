package com.ytjojo.fragmentstack;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;

//http://stackoverflow.com/questions/23504790/android-multiple-fragment-transaction-ordering
class FragmentTransactionBugFixHack {
    private static final String TAG = "FragmentBugFixHack";

    public static ArrayList<Integer> injectAvailIndicesAutoReverseOrder(FragmentManager fragmentManager) {
        try {
            if (fragmentManager == null || !fragmentManager.getClass().getName().contains("FragmentManagerImpl"))
                return null;
            Field field = fragmentManager.getClass().getDeclaredField("mAvailIndices");
            field.setAccessible(true);
            ArrayList<Integer> mAvailIndices = (ArrayList<Integer>) field.get(fragmentManager);
            if (mAvailIndices != null && mAvailIndices instanceof ReverseOrderArrayList)
                return mAvailIndices;
            ArrayList<Integer> backupList = mAvailIndices;
            mAvailIndices = new ReverseOrderArrayList<>();
            field.set(fragmentManager, mAvailIndices);
            if (backupList != null) {
                mAvailIndices.addAll(backupList);
            }
            return mAvailIndices;
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }
//    public static void injectFragmentTransactionAvailIndicesAutoReverseOrder(FragmentManager fragmentManager) {
//        try {
//            Log.d(TAG, "injection injectFragmentTransactionAvailIndicesAutoReverseOrder");
//            if (fragmentManager==null || !(fragmentManager instanceof FragmentManagerImpl)) return;
//            FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
//            if (fragmentManagerImpl.mAvailIndices!=null && fragmentManagerImpl.mAvailIndices instanceof ReverseOrderArrayList) return;
//            ArrayList<Integer> backupList = fragmentManagerImpl.mAvailIndices;
//            fragmentManagerImpl.mAvailIndices = new ReverseOrderArrayList<>();
//            if (backupList!=null) {
//                fragmentManagerImpl.mAvailIndices.addAll(backupList);
//            }
//            Log.d(TAG, "injection ok");
//        } catch (Exception e) {
//            Log.e(TAG, e);
//        }
//    }

//    public static void reorderIndices(FragmentManager fragmentManager) {
//        if (!(fragmentManager instanceof FragmentManagerImpl))
//            return;
//        FragmentManagerImpl fragmentManagerImpl = (FragmentManagerImpl) fragmentManager;
//        if (fragmentManagerImpl.mAvailIndices != null && fragmentManagerImpl.mAvailIndices.size() > 1) {
//            Collections.sort(fragmentManagerImpl.mAvailIndices, Collections.reverseOrder());
//        }
//    }
}
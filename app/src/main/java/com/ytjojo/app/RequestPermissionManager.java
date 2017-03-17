package com.ytjojo.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;

import com.orhanobut.logger.Logger;
import com.ytjojo.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Administrator on 2016/9/11 0011.
 */
public class RequestPermissionManager<T> {
    private static final String TAG = "RequestPermissionManager";
    private static int sRequestCode =1024;

    public RequestPermissionManager(T host){
        this.mHost = host;
        if(mCallbackSparseArray ==null){
            mCallbackSparseArray = new SparseArray<>();
        }
    }
    private T mHost;
    private Activity mActivity;
    private SparseArray<PermissionsCallback> mCallbackSparseArray;
    public static class PermissionHolder{
        public String[] mPermisses;
        private  int mRequestCode;
        public String mSettingMessage;
        public String mRationaleMessage;
        public PermissionsCallback mPermissionsCallback;
        public CreateDialogCallback mCreateDialogCallback;
    }
    public static  int generateRequestCode(){
        return sRequestCode ++;
    }

    public static String[] getReGroup(String ... permission){
        if(permission.length==1){
            return permission;
        }
        ArrayList<String> list =new ArrayList<>();
        for (String p: permission){
            if(!list.contains(p)){
                list.add(p);
            }
        }
        return list.toArray(new String[list.size()]);
    }
    public static String getGroupPermission(String permission){
        switch (permission){
            case Manifest.permission.WRITE_CONTACTS:
            case Manifest.permission.GET_ACCOUNTS:
            case Manifest.permission.READ_CONTACTS:
                return Manifest.permission.WRITE_CONTACTS;
            case Manifest.permission.READ_CALL_LOG:
            case Manifest.permission.READ_PHONE_STATE:
            case Manifest.permission.CALL_PHONE:
            case Manifest.permission.WRITE_CALL_LOG:
            case Manifest.permission.USE_SIP:
            case Manifest.permission.PROCESS_OUTGOING_CALLS:
            case Manifest.permission.ADD_VOICEMAIL:
                return Manifest.permission.CALL_PHONE;
            case Manifest.permission.READ_CALENDAR:
            case Manifest.permission.WRITE_CALENDAR:
                return  Manifest.permission.WRITE_CALENDAR;
            case Manifest.permission.CAMERA:
                return Manifest.permission.CAMERA;
            case Manifest.permission.BODY_SENSORS:
                return Manifest.permission.BODY_SENSORS;
            case Manifest.permission.ACCESS_FINE_LOCATION:
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return Manifest.permission.BODY_SENSORS;
            case Manifest.permission.READ_EXTERNAL_STORAGE:
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return Manifest.permission.WRITE_EXTERNAL_STORAGE;
            case Manifest.permission.RECORD_AUDIO:
                return Manifest.permission.RECORD_AUDIO;
            case Manifest.permission.READ_SMS:
            case Manifest.permission.RECEIVE_WAP_PUSH:
            case Manifest.permission.RECEIVE_MMS:
            case Manifest.permission.RECEIVE_SMS:
            case Manifest.permission.SEND_SMS:
                return Manifest.permission.SEND_SMS;
        }
        return null;
    }

    private Context getContext(){
        if(mActivity !=null){
            return mActivity;
        }
        if(mHost instanceof Activity){
            mActivity = (Activity) mHost;
            return mActivity;
        }
        if(mHost instanceof Fragment){
            return mActivity = ((Fragment)((Fragment) mHost)).getActivity();
        }
        return mActivity = ((android.app.Fragment) mHost).getActivity();
    }
    public static boolean hasPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");
            return true;
        }

        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (!hasPerm) {
                return false;
            }
        }
        return true;
    }
    public static Pair<ArrayList<String>,ArrayList<String>> getGropedPermissions(Context context, String... perms) {
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");
            ArrayList<String> list = new ArrayList(Arrays.asList(perms));
            return new Pair(list,new ArrayList<>());
        }
        ArrayList<String> enabled =new ArrayList<>();
        ArrayList<String> disabled  =new ArrayList<>();
        for (String perm : perms) {
            boolean hasPerm = (ContextCompat.checkSelfPermission(context, perm) ==
                    PackageManager.PERMISSION_GRANTED);
            if (hasPerm) {
                enabled.add(perm);
            }else{
                disabled.add(perm);
            }
        }
        return new Pair<>(enabled,disabled);
    }

    public void requestPermissions(int  requestCode,String rationale,
                                          @StringRes int positiveButton,
                                          @StringRes int negativeButton,
                                          final String... perms) {
        Pair<ArrayList<String>,ArrayList<String>> pair= getGropedPermissions(getContext(),perms);
        ArrayList<String> grantedPerms = pair.first;
        if(grantedPerms.size() ==perms.length){
            dispatchCallbackGranted(requestCode,perms);
        }else{
            boolean shouldShowRationale = false;
            for (String perm : pair.second) {
                shouldShowRationale =
                        shouldShowRationale || shouldShowRequestPermissionRationale(mHost, perm);
            }
            String[] permissiones = pair.second.toArray(new String[pair.second.size()]);
            if (shouldShowRationale) {
                showRationaleDialog(requestCode,rationale,
                        getContext().getString(positiveButton),
                        getContext().getString(negativeButton),
                        permissiones);

            } else {
                executePermissionsRequest(mHost, permissiones, requestCode);
            }
        }

    }
    private void showRationaleDialog(final int requestCode,String rationale,String positiveButton,
                                     String negativeButton,String ... perms){
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setMessage(rationale)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        executePermissionsRequest(mHost, perms, requestCode);
                    }
                })
                .setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // act as if the permissions were denied
                       dispatchCallbackDenied(requestCode);

                    }
                }).create();
        dialog.show();
    }
    private void dispatchCallbackGranted(int requestCode,String ... perms){

    }
    private void dispatchCallbackDenied(int requestCode,String ... perms){

    }

    @TargetApi(23)
    private static boolean shouldShowRequestPermissionRationale(Object object, String perm) {
        if (object instanceof Activity) {
            return ActivityCompat.shouldShowRequestPermissionRationale((Activity) object, perm);
        } else if (object instanceof Fragment) {
            return ((Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else if (object instanceof android.app.Fragment) {
            return ((android.app.Fragment) object).shouldShowRequestPermissionRationale(perm);
        } else {
            return false;
        }
    }

    @TargetApi(23)
    public static void executePermissionsRequest(Object object, String[] perms, int requestCode) {

        if (object instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) object, perms, requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).requestPermissions(perms, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).requestPermissions(perms, requestCode);
        }
    }
    public void onRequestPermissionsResult(int requestCode ,String[] permissions,
                                                  int[] grantResults) {

        Logger.e(permissions.toString());
        // Make a collection of granted and denied permissions from the request.
        ArrayList<String> granted = new ArrayList<>();
        ArrayList<String> denied = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            String perm = permissions[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                granted.add(perm);
            } else {
                denied.add(perm);
            }
        }

        // Report granted permissions, if any.
        if (!granted.isEmpty()) {
            // Notify callbacks
            dispatchCallbackGranted(requestCode,CollectionUtils.listToArray(granted,String[].class));

        }
        // Report denied permissions, if any.
        if (!denied.isEmpty()) {
//            checkDeniedPermissionsNeverAskAgain(requestCode,"去设置啊傻逼", R.string.setting, android.R.string.cancel, null, CollectionUtils.listToArray(denied,String[].class));
            dispatchCallbackDenied(requestCode,CollectionUtils.listToArray(denied,String[].class));
        }
    }

    @TargetApi(11)
    private void startAppSettingsScreen(Object object,
                                               Intent intent,int requestCode) {
        if (object instanceof Activity) {
            ((Activity) object).startActivityForResult(intent,requestCode);
        } else if (object instanceof Fragment) {
            ((Fragment) object).startActivityForResult(intent, requestCode);
        } else if (object instanceof android.app.Fragment) {
            ((android.app.Fragment) object).startActivityForResult(intent, requestCode);
        }
    }

    public boolean checkDeniedPermissionsNeverAskAgain(int requestCode,String rationale,
                                                              @StringRes int positiveButton,
                                                              @StringRes int negativeButton,
                                                              String ... deniedPerms) {
        return checkDeniedPermissionsNeverAskAgain(requestCode, rationale,
                positiveButton, negativeButton, null, deniedPerms);
    }

    public  boolean checkDeniedPermissionsNeverAskAgain(int requestCode,
                                                              String rationale,
                                                              @StringRes int positiveButton,
                                                              @StringRes int negativeButton,
                                                              @Nullable DialogInterface.OnClickListener negativeButtonOnClickListener,
                                                              String ... deniedPerms) {
        boolean shouldShowRationale;
        for (String perm : deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(mHost, perm);
            if (!shouldShowRationale) {
                final Activity activity = (Activity) getContext();
                AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setMessage(rationale)
                        .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                                intent.setData(uri);
                                startAppSettingsScreen(mHost, intent,requestCode);
                            }
                        })
                        .setNegativeButton(negativeButton, negativeButtonOnClickListener)
                        .create();
                dialog.show();

                return true;
            }
        }
        return false;
    }
    public static boolean checkDeniedPermissionsNeverAskAgain(Object host,int requestCode,String ...deniedPerms){
        boolean shouldShowRationale;
        for (String perm : deniedPerms) {
            shouldShowRationale = shouldShowRequestPermissionRationale(host, perm);
            if (!shouldShowRationale) {
                return true;
            }
        }
        return false;
    }
}

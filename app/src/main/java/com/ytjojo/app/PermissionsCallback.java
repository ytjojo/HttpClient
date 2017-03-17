package com.ytjojo.app;

import android.support.v4.app.ActivityCompat;

import java.util.List;

public interface PermissionsCallback extends
        ActivityCompat.OnRequestPermissionsResultCallback {

    void onPermissionsGranted(int requestCode, List<String> perms);
    void onPermissionsDenied(int requestCode, List<String> perms);

}
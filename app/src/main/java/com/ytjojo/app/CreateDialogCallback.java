package com.ytjojo.app;

import android.app.Dialog;

/**
 * Created by Administrator on 2016/9/12 0012.
 */
public interface CreateDialogCallback {

   Dialog showRationaleDialog(int requestCode,String ...perms);
}

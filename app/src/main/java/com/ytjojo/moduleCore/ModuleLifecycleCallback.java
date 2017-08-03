package com.ytjojo.moduleCore;

import android.content.Context;

/**
 * Created by Administrator on 2017/8/1 0001.
 */

public interface ModuleLifecycleCallback {


	void onStop();
	void onCreate(Context c);
	void onLowMemory() ;
}

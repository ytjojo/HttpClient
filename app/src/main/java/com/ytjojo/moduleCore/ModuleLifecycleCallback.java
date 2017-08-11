package com.ytjojo.moduleCore;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/8/1 0001.
 */

public interface ModuleLifecycleCallback {


	void onStop();

	/**
	 * 会判断是否主进程
	 * @param c
	 */
	void onCreate(Context c);
	void onLowMemory() ;

	void dependency();
	void configure();
	void excute();
	void denpendOn(Class<? extends ModuleLifecycleCallback> callback);
	IPlugin findPlugin(Class<? extends ModuleLifecycleCallback> callback);
	<T> void registerService(T service);
	void onLogin();
	void onLogout();
	Application getApplication();
}

package com.ytjojo.moduleCore;

import android.app.Application;

/**
 * Created by Administrator on 2017/8/1 0001.
 */

public interface IPlugin {

	void dependency();
	void configure();
	void excute();
	void denpendOn(Class<? extends IPlugin> pluginClazz);
	IPlugin findPlugin(Class<? extends IPlugin> pluginClazz);
	<T> void registerService(Class<T> clazz,T service);
	void onLogin();
	void onLogout();
	Application getApplication();


}

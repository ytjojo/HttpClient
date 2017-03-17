package com.ytjojo.injection.component;

import android.app.Application;
import android.content.Context;


import com.ytjojo.injection.ApplicationContext;
import com.ytjojo.injection.module.ApplicationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    @ApplicationContext
    Context context();
    Application application();
//        PreferencesHelper preferencesHelper();
//    RibotsService ribotsService();
//    void inject(SyncService syncService);
//    DatabaseHelper databaseHelper();
//    DataManager dataManager();
//    Bus eventBus();

}

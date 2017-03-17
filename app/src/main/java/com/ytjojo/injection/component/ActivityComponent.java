package com.ytjojo.injection.component;

import com.ytjojo.injection.PerActivity;
import com.ytjojo.injection.module.ActivityModule;
import com.ytjojo.ui.MainActivity;
import dagger.Component;


/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(MainActivity mainActivity);

}

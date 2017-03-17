package com.ytjojo.practice.ui;

import android.app.Activity;
import android.support.multidex.MultiDexApplication;
import android.view.Menu;

import com.lody.turbodex.TurboDex;
import com.ytjojo.practice.BuildConfig;
import com.ytjojo.practice.shadows.ShadowMultiDex;
import com.ytjojo.practice.shadows.ShadowTurboDex;
import com.ytjojo.ui.MainActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

/**
 * Created by Administrator on 2016/11/3 0003.
 */
@RunWith(RobolectricGradleTestRunner.class)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@Config(application = MultiDexApplication.class,constants = BuildConfig.class,sdk = 21,packageName = "com.ytjojo.flux",shadows = {ShadowMultiDex.class, ShadowTurboDex.class})
@PrepareForTest(TurboDex.class)
@SuppressStaticInitializationFor("com.lody.turbodex.TurboDex")//阻止静态代码块运行
public class TestActivityTest {
    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Before
    public void setUp(){
//        TurboDexMock.mockTurboDex();
        PowerMockito.mockStatic(TurboDex.class);
    }
    @Test
    public void create() throws Exception{
        PowerMockito.mockStatic(TurboDex.class);
        Activity activity = Robolectric.setupActivity(MainActivity.class);
        final Menu menu = Shadows.shadowOf(activity).getOptionsMenu();
        Assert.assertNull(menu);
//        Assert.assertEquals("Settings",menu.findItem(R.id.action_detail).getTitle());

    }
}

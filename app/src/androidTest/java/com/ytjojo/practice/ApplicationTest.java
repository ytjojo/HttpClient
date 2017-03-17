package com.ytjojo.practice;

import android.app.Application;
import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    Application mBaseApplication;
    public ApplicationTest() {
        super(Application.class);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        mBaseApplication = getApplication();
    }
    @LargeTest
    private void beginTest(){
        Log.e("sssssssssssssssssss","-----------------------");


    }
}
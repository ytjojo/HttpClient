package com.ytjojo.practice;

import com.lody.turbodex.TurboDex;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;

import java.util.ArrayList;

public class MyTestRunner  extends RobolectricGradleTestRunner {


    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public MyTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }
    @Override
    public InstrumentationConfiguration createClassLoaderConfig() {
        InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
        /**
         * 添加要进行Shadow的对象
         */
        builder.addInstrumentedPackage(TurboDex.class.getPackage().getName());
        builder.addInstrumentedClass(TurboDex.class.getName());

        return builder.build();
    }
    protected ArrayList<Class<?>> bindShadowClasses() {
       return null;
   }
}
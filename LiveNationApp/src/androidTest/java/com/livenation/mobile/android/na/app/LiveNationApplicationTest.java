package com.livenation.mobile.android.na.app;

import com.livenation.mobile.android.na.app.LiveNationApplication;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

import android.app.Application;
import android.test.InstrumentationTestCase;

/**
 * Created by elodieferrais on 2/4/15.
 */
public class LiveNationApplicationTest extends InstrumentationTestCase {

    private CountDownLatch signal;

    public void testUnregisterInternetStateReceiver() {
        try {
            signal = new CountDownLatch(2);
            final Application application = new LiveNationApplication();
            final Method method = LiveNationApplication.class.getDeclaredMethod("unregisterInternetStateReceiver");
            method.setAccessible(true);

            Thread thread1 = new Thread() {
                public void run() {
                    try {
                        method.invoke(application);
                        signal.countDown();
                    } catch (IllegalAccessException e) {
                        fail(e.getMessage());
                    } catch (InvocationTargetException e) {
                        fail(e.getMessage());
                    }
                }
            };
            Thread thread2 = new Thread() {
                public void run() {
                    try {
                        method.invoke(application);
                        signal.countDown();
                    } catch (IllegalAccessException e) {
                        fail(e.getMessage());
                    } catch (InvocationTargetException e) {
                        fail(e.getMessage());
                    }
                }
            };
            thread1.start();
            thread2.start();

        } catch (NoSuchMethodException e) {
            fail(e.getMessage());
        }

        try {
            signal.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }

    }
}

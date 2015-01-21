package ui;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.na.ui.UrlActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by elodieferrais on 1/21/15.
 */
public class UrlActivityTest extends ActivityInstrumentationTestCase2 {
    private Activity mActivity;

    public UrlActivityTest() {
        super(UrlActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }

    public void testAppWasOpenedFromBrowserSuccess() {
        String referrerToTest = "http://www.livenation.com/artists/41712/new-kids-on-the-block";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasOpenedFromBrowser", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertTrue(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasOpenedFromBrowserFailure() {
        String referrerToTest = "android-app://com.livenation.mobile.android.na/livenation//navigate/art_41712?st=google";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasOpenedFromBrowser", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertFalse(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasOpenedFromGoogleAppSuccess() {
        String referrerToTest = "android-app://com.google.android.googlequicksearchbox/https/www.google.com";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasOpenedFromGoogleApp", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertTrue(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasOpenedFromGoogleAppFailure() {
        String referrerToTest = "android-app://com.google.appcrawler";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasOpenedFromGoogleAppFailure", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertFalse(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasOpenedFromAppSuccess() {
        String referrerToTest = "android-app://com.livenation.mobile.android.na/livenation//navigate/art_41712?st=google";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasOpenedFromApp", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertTrue(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasOpenedFromAppFailure() {
        String referrerToTest = "http://www.livenation.com/artists/41712/new-kids-on-the-block";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasOpenedFromApp", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertFalse(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasCoveredByGoogleCrawlerSuccess() {
        String referrerToTest = "android-app://com.google.appcrawler";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasCoveredByGoogleCrawler", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertTrue(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testAppWasCoveredByGoogleCrawlerFailure() {
        String referrerToTest = "android-app://com.livenation.mobile.android.na/livenation//navigate/art_41712?st=google";
        try {
            Method method = UrlActivity.class.getDeclaredMethod("appWasCoveredByGoogleCrawler", String.class);
            method.setAccessible(true);
            Boolean result = (Boolean) method.invoke(mActivity, referrerToTest);
            assertFalse(result);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }



}

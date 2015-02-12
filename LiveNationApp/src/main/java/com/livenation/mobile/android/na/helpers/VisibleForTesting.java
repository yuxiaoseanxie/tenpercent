package com.livenation.mobile.android.na.helpers;

/**
 * Created by cchilton on 1/13/15.
 * <p/>
 * An annotation that can be used to prevent Proguard from removing class methods that it thinks are not being used.
 * <p/>
 * If a class method is not used by the app, but is used by a test, Proguard will remove that class memmber during its minimize phase.
 * <p/>
 * This results in a "method not found" runtime error when running the tests. Marking the method with this annotation prevents this.
 */
public @interface VisibleForTesting {
}

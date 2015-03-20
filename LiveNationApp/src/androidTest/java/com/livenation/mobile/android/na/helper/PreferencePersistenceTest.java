package com.livenation.mobile.android.na.helper;

import android.mobile.livenation.com.livenationui.persistence.PreferencePersistence;

import android.test.InstrumentationTestCase;
import tools.TestTools;

/**
 * Created by elodieferrais on 3/31/14.
 */
public class PreferencePersistenceTest extends InstrumentationTestCase {

    private static final String NAME = "test_name";
    private PreferencePersistence preferencePersistence;

    @Override
    protected void tearDown() throws Exception {
        TestTools.closeAllActivities(getInstrumentation());
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferencePersistence = new PreferencePersistence(NAME, getInstrumentation().getContext());
    }

    public void testWriteAndReadSuccess() {
        final String KEY = "key_test";
        final String VALUE = "value_test";
        preferencePersistence.write(KEY, VALUE);

        String readValue = preferencePersistence.readString(KEY);
        assertEquals(VALUE, readValue);
    }

    public void testReadFailed() {
        final String KEY = "key_test_which_does_not_exist";

        String readValue = preferencePersistence.readString(KEY);
        assertNull(readValue);
    }

    /**public void testResetSuccess() {
     final String KEY = "key_test";
     final String VALUE = "value_test";
     preferencePersistence.write(KEY, VALUE);
     preferencePersistence.reset();

     String readValue = preferencePersistence.readString(KEY);
     assertNull(null, readValue);
     }**/

}

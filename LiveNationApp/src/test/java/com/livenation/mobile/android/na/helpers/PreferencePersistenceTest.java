package com.livenation.mobile.android.na.helpers;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.ui.TestActivity;

/**
 * Created by elodieferrais on 3/31/14.
 */
public class PreferencePersistenceTest extends ActivityInstrumentationTestCase2 {

    private static final String NAME = "test_name";
    private final PreferencePersistence preferencePersistence = new PreferencePersistence(NAME);
    public PreferencePersistenceTest() {
        super(TestActivity.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        preferencePersistence.reset(getActivity());
    }

    public void testWriteAndReadSuccess() {
        final String KEY = "key_test";
        final String VALUE = "value_test";
        preferencePersistence.write(KEY, VALUE, getActivity());

        String readValue = preferencePersistence.read(KEY, getActivity());
        assertEquals(VALUE, readValue);
    }

    public void testReadFailed() {
        final String KEY = "key_test_which_does_not_exist";

        String readValue = preferencePersistence.read(KEY, getActivity());
        assertNull(readValue);
    }

    public void testResetSuccess() {
        final String KEY = "key_test";
        final String VALUE = "value_test";
        preferencePersistence.write(KEY, VALUE, getActivity());
        preferencePersistence.reset(getActivity());

        String readValue = preferencePersistence.read(KEY, getActivity());
        assertNull(null, readValue);
    }

}

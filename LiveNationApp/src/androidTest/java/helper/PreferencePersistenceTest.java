package helper;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.preferences.PreferencePersistence;
import com.livenation.mobile.android.na.ui.TestActivity;

/**
 * Created by elodieferrais on 3/31/14.
 */
public class PreferencePersistenceTest extends ActivityInstrumentationTestCase2 {

    private static final String NAME = "test_name";
    private PreferencePersistence preferencePersistence;

    public PreferencePersistenceTest() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preferencePersistence = new PreferencePersistence(NAME, getActivity());
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

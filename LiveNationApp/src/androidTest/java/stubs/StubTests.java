package stubs;

import android.test.InstrumentationTestCase;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Collections;

import static stubs.StubHttpStack.createBasicHeaders;

public class StubTests extends InstrumentationTestCase {
    private static final String TEST_URL = "http://example.com/test.txt";
    private static final String TEST_BODY = "hello, world!";

    private final StubHttpStack stack = new StubHttpStack();
    private final RequestQueue queue = stack.newRequestQueue();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        stack.stubGet(TEST_URL, Collections.<String, String>emptyMap())
             .setEmulatedLoadTime(200)
             .andReturnString(TEST_BODY, createBasicHeaders("text/plain"), 200);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        stack.clearStubs();
    }


    public void testSuccessfulStub() {
        SyncResponseAdapter<String> adapter = new SyncResponseAdapter<String>();
        queue.add(new StringRequest(Request.Method.GET, TEST_URL, adapter, adapter));
        try {
            String response = adapter.get();
            assertEquals(TEST_BODY, response);
        } catch (VolleyError e) {
            fail("Stubbed request failed " + e.getMessage());
        }
    }
}

package stubs;

import android.test.InstrumentationTestCase;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;

import static stubs.StubHttpStack.createBasicHeaders;
import static stubs.StubHttpStack.emptyHeaders;

public class StubTests extends InstrumentationTestCase {
    private static final String TEST_URL = "http://example.com/test.file";
    private static final String TEST_BODY = "hello, world!";

    private final StubHttpStack stack = new StubHttpStack();
    private final RequestQueue queue = stack.getRequestQueue();

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        stack.clearStubs();
    }


    public void testSuccessfulStringStub() {
        stack.stubGet(TEST_URL, emptyHeaders())
             .andReturnString(TEST_BODY, createBasicHeaders("text/plain"), 200);

        SyncResponseAdapter<String> adapter = new SyncResponseAdapter<String>();
        queue.add(new StringRequest(Request.Method.GET, TEST_URL, adapter, adapter));

        String response = adapter.getOrFail();
        assertEquals(TEST_BODY, response);
    }

    public void testSuccessfulJsonStub() {
        JSONObject stubbedResponse = new JSONObject();
        try {
            stubbedResponse.put("worked", true);
        } catch (JSONException e) {
            fail("stupid JSONObject");
        }
        stack.stubGet(TEST_URL, emptyHeaders())
             .andReturnJson(stubbedResponse, emptyHeaders(), 200);

        SyncResponseAdapter<JSONObject> adapter = new SyncResponseAdapter<JSONObject>();
        queue.add(new JsonObjectRequest(Request.Method.GET, TEST_URL, null, adapter, adapter));

        JSONObject response = adapter.getOrFail();
        assertEquals(stubbedResponse.toString(), response.toString());
    }
}

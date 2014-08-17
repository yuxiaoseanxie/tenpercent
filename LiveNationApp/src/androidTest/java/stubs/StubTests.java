package stubs;

import android.test.InstrumentationTestCase;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import stubs.converters.JacksonJsonConverter;

import static stubs.StubHttpStack.createBasicHeaders;
import static stubs.StubHttpStack.emptyHeaders;

public class StubTests extends InstrumentationTestCase {
    private static final String TEST_URL = "http://example.com/test.file";
    private static final String TEST_BODY = "hello, world!";
    private static final JacksonJsonConverter JACKSON_JSON_CONVERTER = new JacksonJsonConverter(new ObjectMapper());

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

    public void testSuccessfulGetJsonObjectStub() throws Exception {
        JSONObject stubbedResponse = new JSONObject();
        stubbedResponse.put("worked", true);
        stack.stubGet(TEST_URL, emptyHeaders())
             .andReturnJson(stubbedResponse, emptyHeaders(), 200);

        SyncResponseAdapter<JSONObject> adapter = new SyncResponseAdapter<JSONObject>();
        queue.add(new JsonObjectRequest(Request.Method.GET, TEST_URL, null, adapter, adapter));

        JSONObject response = adapter.getOrFail();
        assertEquals(stubbedResponse.toString(), response.toString());
    }

    public void testSuccessfulGetJacksonConverterStub() {
        stack.stubGet(TEST_URL, emptyHeaders())
             .andReturnJson(new TestObject(), JACKSON_JSON_CONVERTER, emptyHeaders(), 200);

        SyncResponseAdapter<JSONObject> adapter = new SyncResponseAdapter<JSONObject>();
        queue.add(new JsonObjectRequest(Request.Method.GET, TEST_URL, null, adapter, adapter));

        JSONObject response = adapter.getOrFail();
        assertTrue(response.optBoolean("worked"));
    }

    public void testSuccessfulPostStub() throws Exception {
        JSONObject postBody = new JSONObject();
        postBody.put("product_id", "12345678");
        postBody.put("action", "BLOW_UP");

        JSONObject responseBody = new JSONObject();
        responseBody.put("destroyed", true);

        stack.stubPost(TEST_URL, emptyHeaders(), postBody)
             .andReturnJson(responseBody, emptyHeaders(), 200);

        SyncResponseAdapter<JSONObject> adapter = new SyncResponseAdapter<JSONObject>();
        queue.add(new JsonObjectRequest(Request.Method.POST, TEST_URL, postBody, adapter, adapter));

        JSONObject response = adapter.getOrFail();
        assertTrue(response.getBoolean("destroyed"));
    }


    private static class TestObject {
        @SuppressWarnings("unused")
        @JsonProperty("worked") boolean worked = true;
    }

    public void testUnstubbedUrls() {
        SyncResponseAdapter<String> adapter = new SyncResponseAdapter<String>();
        queue.add(new StringRequest(Request.Method.GET, "http://this.will/fail", adapter, adapter));

        try {
            //noinspection UnusedDeclaration
            String unused = adapter.get();
        } catch (VolleyError e) {
            return;
        }

        fail("unstubbed URL should have failed.");
    }
}

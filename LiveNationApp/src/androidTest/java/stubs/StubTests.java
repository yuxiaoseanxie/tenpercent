package stubs;

import android.test.InstrumentationTestCase;
import android.widget.Adapter;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpResponse;
import org.apache.http.impl.io.HttpResponseParser;
import org.json.JSONException;
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

    public void testSuccessfulGetJsonObjectStub() {
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

    public void testSuccessfulGetJacksonConverterStub() {
        stack.stubGet(TEST_URL, emptyHeaders())
             .andReturnJson(new TestObject(), JACKSON_JSON_CONVERTER, emptyHeaders(), 200);

        SyncResponseAdapter<JSONObject> adapter = new SyncResponseAdapter<JSONObject>();
        queue.add(new JsonObjectRequest(Request.Method.GET, TEST_URL, null, adapter, adapter));

        JSONObject response = adapter.getOrFail();
        assertTrue(response.optBoolean("worked"));
    }


    private static class TestObject {
        @JsonProperty("worked") boolean worked = true;
    }
}

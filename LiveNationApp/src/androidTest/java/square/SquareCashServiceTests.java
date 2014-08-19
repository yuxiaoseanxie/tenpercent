package square;

import android.support.annotation.Nullable;
import android.test.InstrumentationTestCase;

import com.android.volley.Response;
import com.livenation.mobile.android.na.cash.service.SessionPersistenceProvider;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;

import org.json.JSONObject;

import java.util.Map;

import stubs.StubHttpStack;
import stubs.SyncResponseAdapter;

import static stubs.StubHttpStack.createEmptyHeaders;
import static stubs.StubHttpStack.createHeadersWithContentType;

public class SquareCashServiceTests extends InstrumentationTestCase {
    private final StubHttpStack stack = new StubHttpStack();
    private SquareCashService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.service = new SquareCashService(getInstrumentation().getTargetContext(), stack.getRequestQueue(), new SquareCashService.CustomerIdProvider() {
            @Override
            public void provideSquareCustomerId(Response.Listener<String> onResponse) {
                onResponse.onResponse("this-is-a-test-session");
            }
        }, new SessionPersistenceProvider() {
            private CashSession session = new CashSession();

            @Override
            public @Nullable CashSession loadSession() {
                return session;
            }

            @Override
            public void saveSession(@Nullable CashSession session) {
                this.session = session;
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        stack.clearStubs();
    }


    //region Util

    private static Map<String, String> createHeaders() {
        Map<String, String> headers = createHeadersWithContentType("application/json");
        headers.put("Authorization", "Client a2jqttf932pokmmkp0xtzz8ku 31842a1e8aba240fcc85c20d2ed74f83");
        return headers;
    }

    //endregion


    public void testSessionCreation() throws Exception {
        JSONObject body = new JSONObject("{\"customer_id\":\"this-is-a-test-session\",\"phone_number\":\"1234567890\",\"client_id\":\"a2jqttf932pokmmkp0xtzz8ku\",\"response_type\":\"token\"}");
        JSONObject response = new JSONObject("{\"access_token\": \"123thisisfake\", \"customer_id\": \"this-is-a-test-session\", \"expires_at\": \"2020-01-01T00:00:00Z\", \"token_type\": \"bearer\"}");
        stack.stubPost("http://cash.square-sandbox.com/oauth2/authorize/cash", createHeaders(), body)
             .andReturnJson(response, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashSession> adapter = new SyncResponseAdapter<CashSession>();
        service.startSession(null, "1234567890", adapter);

        CashSession session = adapter.getOrFail();
        assertEquals("123thisisfake", session.getAccessToken());
        assertEquals("2020-01-01T00:00:00Z", session.getExpiresAt());
        assertEquals("this-is-a-test-session", session.getCustomerId());
        assertEquals("bearer", session.getTokenType());
    }
}

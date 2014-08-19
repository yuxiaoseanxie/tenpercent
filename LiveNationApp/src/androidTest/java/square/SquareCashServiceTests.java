package square;

import android.support.annotation.Nullable;
import android.test.InstrumentationTestCase;

import com.android.volley.Response;
import com.livenation.mobile.android.na.cash.service.SessionPersistenceProvider;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Map;

import stubs.StubHttpStack;
import stubs.SyncResponseAdapter;

import static stubs.StubHttpStack.createEmptyHeaders;
import static stubs.StubHttpStack.createHeadersWithContentType;

public class SquareCashServiceTests extends InstrumentationTestCase {
    private final StubHttpStack stack = new StubHttpStack();
    private final MockSessionPersistenceProvider persistenceProvider = new MockSessionPersistenceProvider();
    private final SquareCashService.CustomerIdProvider customerIdProvider = new SquareCashService.CustomerIdProvider() {
        @Override
        public void provideSquareCustomerId(Response.Listener<String> onResponse) {
            onResponse.onResponse("this-is-a-test-session");
        }
    };
    private SquareCashService service;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        persistenceProvider.populate();
        this.service = new SquareCashService(getInstrumentation().getTargetContext(),
                                             stack.getRequestQueue(),
                                             customerIdProvider,
                                             persistenceProvider);
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

    private static class MockSessionPersistenceProvider implements SessionPersistenceProvider {
        private CashSession cashSession = new CashSession();

        public void reset() {
            this.cashSession = new CashSession();
        }

        public void populate() {
            cashSession.setAccessToken("123thisisfake");
            cashSession.setCustomerId("this-is-a-test-session");
            cashSession.setExpiresAt("2020-01-01T00:00:00Z");
            cashSession.setTokenType(CashSession.TOKEN_TYPE_BEARER);
        }

        @Override
        public @Nullable CashSession loadSession() {
            return cashSession;
        }

        @Override
        public void saveSession(@Nullable CashSession session) {
            this.cashSession = session;
        }
    }

    //endregion


    public void testSessionCreation() throws Exception {
        persistenceProvider.reset();

        JSONObject body = new JSONObject("{\"customer_id\":\"this-is-a-test-session\",\"phone_number\":\"1234567890\",\"client_id\":\"a2jqttf932pokmmkp0xtzz8ku\",\"response_type\":\"token\"}");
        JSONObject response = new JSONObject("{\"access_token\": \"123thisisfake\", \"customer_id\": \"this-is-a-test-session\", \"expires_at\": \"2020-01-01T00:00:00Z\", \"token_type\": \"bearer\"}");
        stack.stubPost(service.makeUrl("oauth2/authorize/cash", null), createHeaders(), body)
             .andReturnJson(response, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashSession> adapter = new SyncResponseAdapter<CashSession>();
        service.startSession(null, "1234567890", adapter);

        CashSession session = adapter.getOrFail();
        assertEquals("123thisisfake", session.getAccessToken());
        assertEquals("2020-01-01T00:00:00Z", session.getExpiresAt());
        assertEquals("this-is-a-test-session", session.getCustomerId());
        assertEquals(CashSession.TOKEN_TYPE_BEARER, session.getTokenType());
    }

    public void testCustomerStatus() throws Exception {
        JSONObject response = new JSONObject("{\"payments\":[],\"blockers\":{\"url\":\"https://cash.square-sandbox.com/cash/enroll/c7zay6myrvqabdbgb0hfcb5i9\",\"card\":{},\"phone_number\":{}},\"passcode_confirmation_enabled\":false}");
        stack.stubGet(service.makeUrl(service.makeRoute("/cash"), null), createHeaders())
             .andReturnJson(response, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashCustomerStatus> adapter = new SyncResponseAdapter<CashCustomerStatus>();
        service.retrieveCustomerStatus(adapter);

        CashCustomerStatus status = adapter.getOrFail();
        assertNotNull(status);
        assertEquals(Collections.<CashPayment>emptyList(), status.getPayments());
        assertNotNull(status.getBlockers());
        assertEquals(status.getBlockers().getUrl(), "https://cash.square-sandbox.com/cash/enroll/c7zay6myrvqabdbgb0hfcb5i9");
        assertNotNull(status.getBlockers().getCard());
        assertNotNull(status.getBlockers().getPhoneNumber());
        assertNull(status.getBlockers().getPasscodeVerification());
        assertFalse(status.isPasswordConfirmationEnabled());
    }
}

package square;

import android.support.annotation.Nullable;
import android.test.InstrumentationTestCase;

import com.android.volley.Response;
import com.livenation.mobile.android.na.cash.service.SessionPersistenceProvider;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import stubs.StubHttpStack;
import stubs.SyncResponseAdapter;

import static stubs.StubHttpStack.createEmptyHeaders;
import static stubs.StubHttpStack.createHeadersWithContentType;

public class SquareCashServiceTests extends InstrumentationTestCase {
    private final StubHttpStack stack = new StubHttpStack();
    private final MockSessionPersistenceProvider persistenceProvider = new MockSessionPersistenceProvider();
    private SquareCashService service;

    private final JSONObject EMPTY_JSON = new JSONObject();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        this.service = new SquareCashService(getInstrumentation().getTargetContext(),
                                             stack.getRequestQueue(),
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
        headers.put("Authorization", "Bearer 123thisisfake");
        return headers;
    }

    private String makeRouteUrl(String route) {
        return service.makeUrl(service.makeRoute(route), null);
    }

    private static class MockSessionPersistenceProvider implements SessionPersistenceProvider {
        private CashSession cashSession = new CashSession();

        public MockSessionPersistenceProvider() {
            cashSession.setAccessToken("123thisisfake");
            cashSession.setCustomerId("this-is-a-test-session");
            cashSession.setExpiresAt(new Date((long)(System.currentTimeMillis() * 1.2)));
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


    public void testCustomerStatus() throws Exception {
        JSONObject response = new JSONObject("{\"payments\":[],\"blockers\":{\"url\":\"https://cash.square-sandbox.com/cash/enroll/c7zay6myrvqabdbgb0hfcb5i9\",\"card\":{},\"phone_number\":{}},\"passcode_confirmation_enabled\":false,\"full_name\":\"John Doe\"}");
        stack.stubGet(makeRouteUrl("/cash"), createHeaders())
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
        assertEquals("John Doe", status.getFullName());
        assertFalse(status.isPasswordConfirmationEnabled());
    }

    public void testUpdateUserFullName() throws Exception {
        JSONObject json = new JSONObject("{\"full_name\": \"John Doe\"}");
        stack.stubPost(makeRouteUrl("/cash/name"), createHeaders(), json)
             .andReturnJson(EMPTY_JSON, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashResponse> adapter = new SyncResponseAdapter<CashResponse>();
        service.updateUserFullName("John Doe", adapter);

        assertNotNull(adapter.getOrFail());
    }

    public void testRequestPhoneVerification() throws Exception {
        JSONObject json = new JSONObject("{\"phone_number\": \"123456789\"}");
        stack.stubPost(makeRouteUrl("/cash/phone-number"), createHeaders(), json)
             .andReturnJson(EMPTY_JSON, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashResponse> adapter = new SyncResponseAdapter<CashResponse>();
        service.requestPhoneVerification("123456789", adapter);
        assertNotNull(adapter.getOrFail());
    }

    public void testVerifyPhoneNumber() throws Exception {
        JSONObject json = new JSONObject("{\"phone_number\": \"123456789\", \"verification_code\": \"123456\"}");
        stack.stubPost(makeRouteUrl("/cash/phone-verification"), createHeaders(), json)
             .andReturnJson(EMPTY_JSON, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashResponse> adapter = new SyncResponseAdapter<CashResponse>();
        service.verifyPhoneNumber("123456789", "123456", adapter);
    }
}

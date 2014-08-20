package square;

import android.support.annotation.Nullable;
import android.test.InstrumentationTestCase;

import com.android.volley.Response;
import com.livenation.mobile.android.na.cash.service.SessionPersistenceProvider;
import com.livenation.mobile.android.na.cash.service.SquareCashService;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkInfo;
import com.livenation.mobile.android.na.cash.service.responses.CashCardLinkResponse;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomer;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomerStatus;
import com.livenation.mobile.android.na.cash.service.responses.CashCustomization;
import com.livenation.mobile.android.na.cash.service.responses.CashMoney;
import com.livenation.mobile.android.na.cash.service.responses.CashPayment;
import com.livenation.mobile.android.na.cash.service.responses.CashResponse;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;

import org.json.JSONObject;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import stubs.StubHttpStack;
import stubs.SyncResponseAdapter;
import stubs.converters.JacksonJsonConverter;
import stubs.converters.JsonConverter;

import static stubs.StubHttpStack.createEmptyHeaders;
import static stubs.StubHttpStack.createHeadersWithContentType;

public class SquareCashServiceTests extends InstrumentationTestCase {
    private final StubHttpStack stack = new StubHttpStack();
    private final MockSessionPersistenceProvider persistenceProvider = new MockSessionPersistenceProvider();
    private SquareCashService service;

    private final JSONObject EMPTY_JSON = new JSONObject();
    private final JsonConverter JSON_CONVERTER = new JacksonJsonConverter(CashResponse.OBJECT_MAPPER);

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


    //region Sessions

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

        assertNotNull(adapter.getOrFail());
    }

    //endregion


    //region Cards

    public void testLinkCard() throws Exception {
        CashCardLinkInfo cardLinkInfo = new CashCardLinkInfo();
        cardLinkInfo.setPostalCode("94158");
        cardLinkInfo.setSecurityCode("155");
        cardLinkInfo.setExpiration("12", "16");
        cardLinkInfo.setNumber("4000000044440000");

        CashCardLinkResponse mockResponse = new CashCardLinkResponse();

        stack.stubPost(makeRouteUrl("/cash/card"), createHeaders(), cardLinkInfo, JSON_CONVERTER)
             .andReturnJson(mockResponse, JSON_CONVERTER, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashCardLinkResponse> adapter = new SyncResponseAdapter<CashCardLinkResponse>();
        service.linkCard(cardLinkInfo, adapter);

        assertNotNull(adapter.getOrFail());
    }

    public void testUnlinkCard() throws Exception {
        stack.stubDelete(makeRouteUrl("/cash/card"), createHeaders())
             .andReturnJson(EMPTY_JSON, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashResponse> adapter = new SyncResponseAdapter<CashResponse>();
        service.unlinkCard(adapter);

        assertNotNull(adapter.getOrFail());
    }

    public void testInitiatePayment() throws Exception {
        CashCustomer sender = new CashCustomer();
        sender.setPhoneNumber("123456789");

        CashPayment payment = CashPayment.newRequest();
        payment.setAmount(CashMoney.newUSD(1000));
        payment.setSender(sender);
        payment.setSenderCustomization(new CashCustomization("Your share of Katy Perry", "Katy Perry at Shoreline Amphitheater on THE WORLD OF TOMORROW"));

        stack.stubPost(makeRouteUrl("/cash/payments"), createHeaders(), payment, JSON_CONVERTER)
             .andReturnJson(payment, JSON_CONVERTER, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashPayment> adapter = new SyncResponseAdapter<CashPayment>();
        service.initiatePayment(payment, adapter);

        assertNotNull(adapter.getOrFail());
    }

    public void testRetrievePayment() throws Exception {
        CashCustomer sender = new CashCustomer();
        sender.setPhoneNumber("123456789");

        CashPayment payment = CashPayment.newRequest();
        payment.setAmount(CashMoney.newUSD(1000));
        payment.setSender(sender);
        payment.setSenderCustomization(new CashCustomization("Your share of Katy Perry", "Katy Perry at Shoreline Amphitheater on THE WORLD OF TOMORROW"));
        payment.setPaymentId("123");

        stack.stubGet(makeRouteUrl("/cash/payments/123"), createHeaders())
             .andReturnJson(payment, JSON_CONVERTER, createEmptyHeaders(), 200);

        SyncResponseAdapter<CashPayment> adapter = new SyncResponseAdapter<CashPayment>();
        service.retrievePayment("123", adapter);

        CashPayment response = adapter.getOrFail();
        assertNotNull(response);
        assertEquals(response.getPaymentId(), "123");

    }

    //endregion


    //region Component Tests

    public void testServiceAssumptions() {
        assertEquals(service.makeRoute("/test"), "v1/this-is-a-test-session/test");
        assertEquals(service.makeUrl("test", null), "http://cash.square-sandbox.com/test");

        Map<String, String> testParams = new LinkedHashMap<String, String>();
        testParams.put("x", "22");
        testParams.put("y", "19");
        assertEquals(service.makeUrl("test", testParams), "http://cash.square-sandbox.com/test?x=22&y=19");
    }

    public void testErrorResponses() throws Exception {
        JSONObject json = new JSONObject("{\"error\": \"too.many.lollipops\", \"error_description\": \"user has consumed too many lollipops, try again later.\"}");
        stack.stubGet(makeRouteUrl("/cash/payments/123"), createHeaders())
             .andReturnJson(json, createEmptyHeaders(), 400);

        SyncResponseAdapter<CashPayment> adapter = new SyncResponseAdapter<CashPayment>();
        service.retrievePayment("123", adapter);

        try {
            adapter.get();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("too.many.lollipops"));
            assertTrue(e.getMessage().contains("user has consumed too many lollipops, try again later."));

            return;
        }

        fail("Error was not propagated");
    }

    public void testJsonDeserializationAssumptions() {
        try {
            CashResponse.OBJECT_MAPPER.readValue("{\"this\": \"field\", \"does\": \"not\", \"exist\": \"in object\"}", CashResponse.class);
        } catch (Exception e) {
            fail("Unrecognized fields should be ignored.");
        }
    }

    //endregion
}

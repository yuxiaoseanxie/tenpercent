package uber;

import android.test.InstrumentationTestCase;

import com.livenation.mobile.android.na.uber.UberClient;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;
import com.livenation.mobile.android.na.uber.service.model.UberTimeResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.observers.TestSubscriber;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class UberTests extends InstrumentationTestCase {
    private static final float[] LOCATION_SF = {37.7833f, -122.4167f};
    private static final float[] LOCATION_EAST_BAY = {37.5423f, -122.04f};

    private static final float[] LOCATION_ELSEWHERE1 = {39.1889674f, -84.86657f};
    private static final float[] LOCATION_ELSEWHERE2 = {39.095438f, -84.516041f};

    private final String UBER_CLIENT_ID = "ZFneHoL_OZAz8gqY_jiB0deA2GVStQzm";
    private final String UBER_TOKEN = "IIZsYJmrwblTK1W_HsZY62jULS11HbvM1fReo0Bt";
    private UberClient uberClient;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uberClient = new UberClient(getInstrumentation().getContext(), new UberHttpServer(), UBER_CLIENT_ID, UBER_TOKEN);
    }

    public void testGetProducts() {
        Observable<UberProductResponse> products = uberClient.getService().getProducts(LOCATION_SF[0], LOCATION_SF[1]);
        TestSubscriber<UberProductResponse> testSubscriber = new TestSubscriber<>();
        products.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberProductResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getProducts().size() > 0);
    }

    public void testGetEstimates() {
        Observable<UberPriceResponse> prices = uberClient.getService().getEstimates(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1]);
        TestSubscriber<UberPriceResponse> testSubscriber = new TestSubscriber<>();
        prices.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberPriceResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getPrices().size() > 0);
    }

    public void testGetTimes() {
        Observable<UberTimeResponse> times = uberClient.getService().getTimes(LOCATION_SF[0], LOCATION_SF[1]);
        TestSubscriber<UberTimeResponse> testSubscriber = new TestSubscriber<>();
        times.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberTimeResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getTimes().size() > 0);
    }

    public void testGetLiveNationEstimates() {
        Observable<ArrayList<LiveNationEstimate>> estimates = uberClient.getEstimates(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1]);
        TestSubscriber<ArrayList<LiveNationEstimate>> testSubscriber = new TestSubscriber<>();
        estimates.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<ArrayList<LiveNationEstimate>> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        for (LiveNationEstimate estimate : completed.get(0)) {
            assertNotNull(estimate);
        }
    }

    public void testMiddleOfNowhereEstimates() {
        Observable<ArrayList<LiveNationEstimate>> estimates = uberClient.getEstimates(LOCATION_ELSEWHERE1[0], LOCATION_ELSEWHERE1[1], LOCATION_ELSEWHERE2[0], LOCATION_ELSEWHERE2[1]);
        TestSubscriber<ArrayList<LiveNationEstimate>> testSubscriber = new TestSubscriber<>();
        estimates.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<ArrayList<LiveNationEstimate>> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        for (LiveNationEstimate estimate : completed.get(0)) {
            assertNotNull(estimate);
        }
    }

    public static enum ResponseData {
        SF_TIMES("37.7", "-122.4", "estimates/time", "uber/response_times_sf.json"),
        SF_PRODUCTS("37.7", "-122.4", "products", "uber/response_products_sf.json"),
        SF_PRICES("37.7", "-122.4", "estimates/price", "uber/response_prices_sf.json"),
        ELSEWHERE_TIMES("39.1", "-84.8", "estimates/time", "uber/response_times_elsewhere.json"),
        ELSEWHERE_PRODUCTS("39.1", "-84.8", "products", "uber/response_products_elsewhere.json"),
        ELSEWHERE_PRICES("39.1", "-84.8", "estimates/price", "uber/response_prices_elsewhere.json");

        private final String response;
        private final String pattern;
        private final String lat;
        private final String lng;

        ResponseData(String lat, String lng, String pattern, String response) {
            this.response = response;
            this.pattern = pattern;
            this.lat = lat;
            this.lng = lng;
        }

        public static ResponseData getResponse(String url) {
            for (ResponseData data : ResponseData.values()) {
                if (url.contains(data.getPattern()) && url.contains("latitude=" + data.getLat()) && url.contains("longitude=" + data.getLng())) {
                    return data;
                }
            }
            throw new IllegalStateException("Unhandled URL pattern:" + url);
        }

        public String getPattern() {
            return pattern;
        }

        public String getResponse() {
            return response;
        }

        public String getLat() {
            return lat;
        }

        public String getLng() {
            return lng;
        }
    }

    private class UberHttpServer implements Client {

        @Override
        public Response execute(Request request) throws IOException {
            String response = readResponse(ResponseData.getResponse(request.getUrl()).getResponse());
            return new Response(request.getUrl(), 200, "nothing", Collections.EMPTY_LIST, new TypedByteArray("application/json", response.getBytes()));
        }

        private String readResponse(String filename) {
            try {
                InputStream stream = getInstrumentation().getContext().getAssets().open(filename);
                int size = stream.available();
                byte[] buffer = new byte[size];
                stream.read(buffer);
                stream.close();
                String text = new String(buffer);
                return text;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }


    }
}

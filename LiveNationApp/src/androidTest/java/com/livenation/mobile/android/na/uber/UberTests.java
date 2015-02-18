package com.livenation.mobile.android.na.uber;

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

import android.test.InstrumentationTestCase;
import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import rx.Observable;
import rx.Observer;
import rx.observers.TestSubscriber;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class UberTests extends InstrumentationTestCase {
    private static final float[] LOCATION_SF = {37.7833f, -122.4167f};
    private static final float[] LOCATION_EAST_BAY = {37.5423f, -122.04f};

    private final String UBER_CLIENT_ID = "ZFneHoL_OZAz8gqY_jiB0deA2GVStQzm";
    private final String UBER_TOKEN = "IIZsYJmrwblTK1W_HsZY62jULS11HbvM1fReo0Bt";
    private UberClient uberClient;
    UberHttpServer httpServer = new UberHttpServer();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uberClient = new UberClient(getInstrumentation().getContext(), httpServer, UBER_CLIENT_ID, UBER_TOKEN);
    }

    public void testGetProducts() {
        httpServer.setProductfileName("uber/response_products.json");

        Observable<UberProductResponse> products = uberClient.getService().getProducts(LOCATION_SF[0], LOCATION_SF[1]);
        TestSubscriber<UberProductResponse> testSubscriber = new TestSubscriber<>();
        products.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberProductResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getProducts().size() > 0);
    }

    public void testGetEstimates() {
        httpServer.setPricefileName("uber/response_prices.json");

        Observable<UberPriceResponse> prices = uberClient.getService().getEstimates(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1]);
        TestSubscriber<UberPriceResponse> testSubscriber = new TestSubscriber<>();
        prices.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberPriceResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getPrices().size() > 0);
    }

    public void testGetTimes() {
        httpServer.setTimefileName("uber/response_times.json");

        Observable<UberTimeResponse> times = uberClient.getService().getTimes(LOCATION_SF[0], LOCATION_SF[1]);
        TestSubscriber<UberTimeResponse> testSubscriber = new TestSubscriber<>();
        times.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberTimeResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getTimes().size() > 0);
    }

    public void testGetLiveNationEstimates() {
        httpServer.setPricefileName("uber/response_prices.json");
        httpServer.setTimefileName("uber/response_times.json");
        httpServer.setProductfileName("uber/response_products.json");

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

    public void testGetEmptyLiveNationEstimates() {
        httpServer.setPricefileName("uber/response_prices_empty.json");
        httpServer.setTimefileName("uber/response_times_empty.json");
        httpServer.setProductfileName("uber/response_products_empty.json");

        Observable<ArrayList<LiveNationEstimate>> estimates = uberClient.getEstimates(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1]);
        TestSubscriber testSubscriber = new TestSubscriber(new Observer() {
            @Override
            public void onCompleted() {
                fail("This api call should not be completed");
            }

            @Override
            public void onError(Throwable e) {
                assertTrue(true);
            }

            @Override
            public void onNext(Object o) {
            }
        });
        estimates.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<ArrayList<LiveNationEstimate>> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() == 0);
    }

    private class UberHttpServer implements Client {

        String productfileName;
        String timefileName;
        String pricefileName;

        public void setProductfileName(String productfileName) {
            this.productfileName = productfileName;
        }

        public void setTimefileName(String timefileName) {
            this.timefileName = timefileName;
        }

        public void setPricefileName(String pricefileName) {
            this.pricefileName = pricefileName;
        }

        @Override
        public Response execute(Request request) throws IOException {
            String fileName = null;
            if (request.getUrl().contains("products")) {
                fileName = productfileName;
            } else if (request.getUrl().contains("price")) {
                fileName = pricefileName;
            } else if (request.getUrl().contains("time")) {
                fileName = timefileName;
            }
            String response = readResponse(fileName);
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

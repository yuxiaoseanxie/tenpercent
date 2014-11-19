package com.livenation.mobile.android.na.uber;

import android.content.Intent;
import android.net.Uri;
import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.uber.activities.UberExampleActivity;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.observers.TestSubscriber;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberActivityTest extends ActivityInstrumentationTestCase2<UberExampleActivity> {

    private UberService uberService;
    private static final float[] LOCATION_SF = {37.7833f, -122.4167f};
    private static final float[] LOCATION_EAST_BAY = {37.5423f, -122.04f};

    public UberActivityTest() {
        super(UberExampleActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        getActivity();
        uberService = UberHelper.getUberService();
    }

    public void testUberLaunch() {
        assertTrue(UberHelper.isUberAppInstalled(getActivity()));
        Observable<UberProductResponse> products = uberService.getProducts(LOCATION_SF[0], LOCATION_SF[1]);
        TestSubscriber<UberProductResponse> testSubscriber = new TestSubscriber<UberProductResponse>();
        products.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberProductResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getProducts().size() > 0);

        String productId = completed.get(0).getProducts().get(0).getProductId();
        String clientId = getActivity().getString(R.string.uber_client_id);

        Uri uri = UberHelper.getUberLaunchUri(clientId, productId, LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1], LOCATION_SF[0], LOCATION_SF[1], "Live Nation Labs", "340 Brannan St San Francisco, CA 94107");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        getActivity().startActivity(intent);
        //how to assert uber app opened?
        //how to assert uber app received our destination address?

        assertTrue(true);
    }
}

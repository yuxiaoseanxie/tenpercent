package com.livenation.mobile.android.na.uber;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.livenation.mobile.android.na.uber.service.UberHelper;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    private UberService uberService;
    private static final float[] LOCATION_SF = {37.7833f, -122.4167f};
    private static final float[] LOCATION_EAST_BAY = {37.5423f, -122.04f};

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        uberService = UberHelper.getUberService();
    }

    public void testGetProducts() {
        Observable<UberProductResponse> products = uberService.getProducts(LOCATION_SF[0], LOCATION_SF[1]);
        TestSubscriber<UberProductResponse> testSubscriber = new TestSubscriber<UberProductResponse>();
        products.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberProductResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getProducts().size() > 0);
    }

    public void testGetEstimates() {
        Observable<UberPriceResponse> prices = uberService.getEstimates(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1]);
        TestSubscriber<UberPriceResponse> testSubscriber = new TestSubscriber<UberPriceResponse>();
        prices.subscribe(testSubscriber);
        testSubscriber.awaitTerminalEvent(10, TimeUnit.SECONDS);
        List<UberPriceResponse> completed = testSubscriber.getOnNextEvents();
        assertTrue(completed.size() > 0);
        assertTrue(completed.get(0).getPrices().size() > 0);
    }
}


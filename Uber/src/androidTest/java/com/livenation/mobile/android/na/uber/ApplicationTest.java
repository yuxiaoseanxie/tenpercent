package com.livenation.mobile.android.na.uber;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.livenation.mobile.android.na.uber.service.UberHelper;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.UberPrices;
import com.livenation.mobile.android.na.uber.service.model.UberProducts;

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
        UberProducts products = uberService.getProducts(LOCATION_SF[0], LOCATION_SF[1]);
        assertNotNull(products);
        assertTrue(products.getProducts().size() > 0);
    }

    public void testGetEstimates() {
        UberPrices prices = uberService.getEstimates(LOCATION_SF[0], LOCATION_SF[1], LOCATION_EAST_BAY[0], LOCATION_EAST_BAY[1]);
        assertNotNull(prices);
        assertTrue(prices.getPrices().size() > 0);
    }
}


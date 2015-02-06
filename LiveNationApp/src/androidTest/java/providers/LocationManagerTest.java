package providers;

import android.test.InstrumentationTestCase;

import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.concurrent.CountDownLatch;

import mock.LocationProviderMock;

/**
 * Created by elodieferrais on 2/4/15.
 */
public class LocationManagerTest extends InstrumentationTestCase {

    private LocationManager locationManager;
    private CountDownLatch signal;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        locationManager = new LocationManager(getInstrumentation().getContext());
        locationManager.clearLocationMode(getInstrumentation().getContext());
        signal = new CountDownLatch(1);
    }


    public void testGetLocationWithNoMode() {

        LocationProviderMock provider = new LocationProviderMock();
        final Double[] newLoc = {1d, 1d};
        provider.setLocation(newLoc);
        locationManager.setSystemLocationProvider(provider);

        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(newLoc.equals(response));
                assertTrue(locationManager.getLocationMode() == LocationManager.MODE_SYSTEM);
                signal.countDown();
            }

            @Override
            public void onErrorResponse() {
                fail();
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testGetLocationWithSystemLocationProviderBroken() {
        LocationProviderMock systemProvider = new LocationProviderMock();
        systemProvider.setSuccessFull(false);

        LocationProviderMock userProvider = new LocationProviderMock();
        final Double[] newLoc = {2d, 2d};
        userProvider.setLocation(newLoc);
        locationManager.setSystemLocationProvider(systemProvider);
        locationManager.setUserLocationProvider(userProvider);

        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(LocationManager.DEFAULT_LOCATION.equals(response));
                assertTrue(locationManager.getLocationMode() == LocationManager.MODE_UNKNOWN_BECAUSE_ERROR);

                signal.countDown();
            }

            @Override
            public void onErrorResponse() {
                fail();
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testGetLocationWithSystemLocationProvider() {
        LocationProviderMock systemProvider = new LocationProviderMock();
        final Double[] newLoc = {10d, 10d};
        systemProvider.setLocation(newLoc);
        locationManager.setSystemLocationProvider(systemProvider);

        locationManager.setLocationMode(LocationManager.MODE_SYSTEM, getInstrumentation().getContext());
        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(newLoc.equals(response));
                assertTrue(locationManager.getLocationMode() == LocationManager.MODE_SYSTEM);

                signal.countDown();
            }

            @Override
            public void onErrorResponse() {
                fail();
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }


    public void testGetLocationWithUserLocationProvider() {
        LocationProviderMock systemProvider = new LocationProviderMock();
        final Double[] newSystemLoc = {3d, 3d};
        systemProvider.setLocation(newSystemLoc);

        LocationProviderMock userProvider = new LocationProviderMock();
        final Double[] newUserLoc = {4d, 4d};
        userProvider.setLocation(newUserLoc);
        locationManager.setSystemLocationProvider(systemProvider);
        locationManager.setUserLocationProvider(userProvider);

        locationManager.setLocationMode(LocationManager.MODE_USER, getInstrumentation().getContext());
        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(newUserLoc.equals(response));
                assertTrue(locationManager.getLocationMode() == LocationManager.MODE_USER);
                signal.countDown();
            }

            @Override
            public void onErrorResponse() {
                fail();
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testGetLocationWithUnknownMode() {
        LocationProviderMock systemProvider = new LocationProviderMock();
        final Double[] newSystemLoc = {3d, 3d};
        systemProvider.setLocation(newSystemLoc);

        LocationProviderMock userProvider = new LocationProviderMock();
        final Double[] newUserLoc = {4d, 4d};
        userProvider.setLocation(newUserLoc);
        locationManager.setSystemLocationProvider(systemProvider);
        locationManager.setUserLocationProvider(userProvider);

        locationManager.setLocationMode(LocationManager.MODE_UNKNOWN_BECAUSE_ERROR, getInstrumentation().getContext());
        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(newSystemLoc.equals(response));
                signal.countDown();
            }

            @Override
            public void onErrorResponse() {
                fail();
                signal.countDown();
            }
        });

        try {
            signal.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}

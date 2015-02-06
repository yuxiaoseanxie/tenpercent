package providers;

import android.test.InstrumentationTestCase;
import android.util.Log;

import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.concurrent.CountDownLatch;

import mock.LocationManagerWithReverseGeocodeMocked;
import mock.LocationProviderMock;
import mock.ReverseGeocodeMock;

/**
 * Created by elodieferrais on 2/4/15.
 */
public class LocationManagerTest extends InstrumentationTestCase {

    private LocationManager locationManager;
    private CountDownLatch signal;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        locationManager = new LocationManagerWithReverseGeocodeMocked(getInstrumentation().getContext());
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
                assertTrue(response[0].equals(newLoc[0]));
                assertTrue(response[1].equals(newLoc[1]));
                assertEquals(locationManager.getMode(),LocationManager.MODE_SYSTEM);
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
        locationManager.setSystemLocationProvider(systemProvider);

        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(LocationManager.DEFAULT_LOCATION[0].equals(response[0]));
                assertTrue(LocationManager.DEFAULT_LOCATION[1].equals(response[1]));
                assertTrue(locationManager.getMode() == LocationManager.MODE_UNKNOWN_BECAUSE_ERROR);

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

        locationManager.setLocationMode(LocationManager.MODE_SYSTEM);
        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(newLoc[0].equals(response[0]));
                assertTrue(newLoc[1].equals(response[1]));
                assertTrue(locationManager.getMode() == LocationManager.MODE_SYSTEM);

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

        locationManager.setSystemLocationProvider(systemProvider);

        City city = new City("AA", 4d, 4d);
        locationManager.addLocationHistory(city);
        locationManager.setLocationMode(LocationManager.MODE_USER);
        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                Double[] gpsCoor = {4d,4d};
                assertTrue(gpsCoor[0].equals(response[0]));
                assertTrue(gpsCoor[1].equals(response[1]));

                assertTrue(locationManager.getMode() == LocationManager.MODE_USER);
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
        locationManager.setSystemLocationProvider(systemProvider);

        locationManager.setLocationMode(LocationManager.MODE_UNKNOWN_BECAUSE_ERROR);
        locationManager.getLocation(new ProviderCallback<Double[]>() {
            @Override
            public void onResponse(Double[] response) {
                assertTrue(newSystemLoc[0].equals(response[0]));
                assertTrue(newSystemLoc[1].equals(response[1]));
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

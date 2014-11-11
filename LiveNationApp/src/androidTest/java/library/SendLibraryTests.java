package library;

import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.proxy.LiveNationProxy;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibrary;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.LibraryAffinitiesParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;

import mock.AccessTokenProviderMock;
import mock.DeviceIdProviderMock;
import mock.EnvironmentProviderMock;
import mock.LocationProviderMock;

import java.util.concurrent.CountDownLatch;

public class SendLibraryTests extends ActivityInstrumentationTestCase2<TestActivity>{
    private LiveNationProxy proxy;

    public SendLibraryTests() {
        super(TestActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        LiveNationLibrary.start(getActivity(), new EnvironmentProviderMock(), new DeviceIdProviderMock(), new LocationProviderMock(), new AccessTokenProviderMock(),  null);

        this.proxy = new LiveNationProxy();
    }

    public void testSendAffinitiesWithOnSuccessResponse() {
        MusicLibrary musicLibrary = new MusicLibrary();
        MusicLibraryEntry musicLibraryEntry = new MusicLibraryEntry("U2");
        musicLibraryEntry.setPlayCount(2);
        musicLibraryEntry.setTotalSongs(3);
        musicLibrary.add(musicLibraryEntry);
        LibraryAffinitiesParameters parameters = new LibraryAffinitiesParameters();
        parameters.setLibraryDump(musicLibrary);

        final CountDownLatch startApiCall = new CountDownLatch(1);
        proxy.sendLibraryAffinities(parameters, new BasicApiCallback<Void>() {
            @Override
            public void onResponse(Void response) {
                assertTrue(true);
                startApiCall.countDown();
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                assertTrue(false);
                startApiCall.countDown();
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}

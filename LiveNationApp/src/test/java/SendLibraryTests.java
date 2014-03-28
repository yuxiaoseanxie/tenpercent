import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.DummySsoProvider;
import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.ContextConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.SsoProviderConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.StringValueConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryEntry;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * Created by elodieferrais on 3/26/14.
 */
public class SendLibraryTests extends ActivityInstrumentationTestCase2 implements ApiBuilder.OnBuildListener {
    private LiveNationApiService apiService;

    public SendLibraryTests() {
        super(TestActivity.class);
    }


    public void testDefaultConfig() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.build(SendLibraryTests.this);
        block();
        assertTrue(builder.isConfigured());
        assertNotNull(apiService);
    }

    public void testSendAffinitiesWithOnSuccesResponse() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.build(SendLibraryTests.this);
        block();
        LibraryDump libraryDump = new LibraryDump();
        LibraryEntry libraryEntry = new LibraryEntry("U2");
        libraryEntry.setPlayCount(2);
        libraryEntry.setTotalSongs(3);
        libraryDump.add(libraryEntry);

        final CountDownLatch startApiCall = new CountDownLatch(1);
        apiService.sendLibraryAffinities(libraryDump, new LiveNationApiService.SendLibraryAffinitiesCallback() {
            @Override
            public void onSuccess(int artistCount) {
                startApiCall.countDown();
                assert true;
            }

            @Override
            public void onFailure(int errorCode, String message) {
                startApiCall.countDown();
                fail("onFailure method: error code:" + String.valueOf(errorCode) + "\nmessage: " + message);
            }
        });
        try {
            startApiCall.await();
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Override
    public void onApiBuilt(LiveNationApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void onApiAlreadyBuilding() {

    }

    private void block() {
        while (null == apiService) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private LiveNationApiBuilder getApiBuilder() {
        ApiBuilderElement<String> host = new StringValueConfig("http://stg.api.livenation.com");
        ApiBuilderElement<String> deviceId = new StringValueConfig(UUID.randomUUID().toString());
        ApiBuilderElement<String> clientId = new StringValueConfig(Constants.clientId);
        ApiBuilderElement<Context> appContext = new ContextConfig(getInstrumentation().getContext());
        ApiBuilderElement<ApiSsoProvider> ssoProvider = new SsoProviderConfig();
        ApiBuilderElement<Double[]> location = new ApiBuilderElement<Double[]>() {
            @Override
            public void run() {
                Double[] result = new Double[2];
                result[0] = 37d;
                result[1] = -122d;
                setResult(result);
            }
        };
        ssoProvider.setResult(new DummySsoProvider());

        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(host, clientId, deviceId, ssoProvider, location, appContext);

        WeakReference<Activity> weakActivity = new WeakReference<Activity>(getActivity());
        apiBuilder.getActivity().setResult(weakActivity);

        apiBuilder.getAppContext().setResult(weakActivity.get().getApplicationContext());

        return apiBuilder;
    }
}

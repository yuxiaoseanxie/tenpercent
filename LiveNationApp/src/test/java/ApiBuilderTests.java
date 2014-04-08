import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.test.ActivityInstrumentationTestCase2;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.helpers.DummySsoProvider;
import com.livenation.mobile.android.na.ui.HomeActivity;
import com.livenation.mobile.android.na.ui.TestActivity;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.ContextConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.LiveNationApiBuilder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.SsoProviderConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.config.StringValueConfig;
import com.livenation.mobile.android.platform.api.transport.ApiBuilder;
import com.livenation.mobile.android.platform.api.transport.ApiBuilderElement;
import com.livenation.mobile.android.platform.api.transport.ApiSsoProvider;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * Created by cchilton on 3/7/14.
 */
public class ApiBuilderTests extends ActivityInstrumentationTestCase2 implements ApiBuilder.OnBuildListener {
    private LiveNationApiService apiService;

    public ApiBuilderTests() {
        super(TestActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apiService = null;
    }

    public void testApiBuilder() {
        LiveNationApiBuilder builder = getApiBuilder();
        assertNotNull(builder);
    }

    public void testSsoProviderFailure() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.getSsoProvider().setResult(null);
        builder.build(ApiBuilderTests.this);
        assertTrue(!builder.getSsoProvider().hasResult());
    }

    public void testDefaultConfig() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.build(ApiBuilderTests.this);
        block(builder);
        assertTrue(builder.isConfigured());
    }

    public void testGetsAccessToken() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.build(ApiBuilderTests.this);
        block(builder);
        assertTrue(builder.isConfigured());
        assertTrue(builder.getAccessToken().hasResult());
    }

    public void testInvalidateSession() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.build(ApiBuilderTests.this);

        block(builder);
        assertTrue(builder.isConfigured());
        assertTrue(builder.getAccessToken().hasResult());
        String firstToken = builder.getAccessToken().getResult().getToken();
        assertTrue(firstToken.equals(apiService.getApiConfig().getAccessToken()));

        builder.invalidateApiSession();
        builder.getSsoProvider().setResult(new DummySsoProvider());
        assertFalse(builder.isConfigured());
        builder.build(ApiBuilderTests.this);

        this.apiService = null;
        block(builder);
        assertTrue(builder.isConfigured());
        assertTrue(builder.getAccessToken().hasResult());
        String secondToken = builder.getAccessToken().getResult().getToken();

        assertTrue(secondToken.equals(apiService.getApiConfig().getAccessToken()));
        assertFalse(firstToken.equals(secondToken));
    }

    public void testSsoProviderDependentsFailure() {
        LiveNationApiBuilder builder = getApiBuilder();
        builder.getSsoProvider().setResult(null);
        builder.build(ApiBuilderTests.this);
        assertTrue(!builder.getSsoProvider().hasResult());
        assertFalse(builder.isConfigured());
        assertTrue(builder.getNotConfigured().contains(builder.getSsoProvider()));
    }

    @Override
    public void onApiBuilt(LiveNationApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void onApiAlreadyBuilding() {

    }

    private void block(ApiBuilder builder) {
        while (null == apiService) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private LiveNationApiBuilder getApiBuilder() {
        Constants.Environment environment = Constants.Environment.StagingDirect;
        ApiBuilderElement<String> host = new StringValueConfig(environment.getHost());
        ApiBuilderElement<String> deviceId = new StringValueConfig(UUID.randomUUID().toString());
        ApiBuilderElement<String> clientId = new StringValueConfig(environment.getClientId());
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

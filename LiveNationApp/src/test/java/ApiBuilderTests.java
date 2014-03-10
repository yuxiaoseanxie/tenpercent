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
        ApiBuilderElement<String> deviceId = new StringValueConfig(UUID.randomUUID().toString());
        ApiBuilderElement<String> clientId = new StringValueConfig(Constants.clientId);
        ApiBuilderElement<Context> appContext = new ContextConfig(getInstrumentation().getContext());
        ApiBuilderElement<ApiSsoProvider> ssoProvider = new SsoProviderConfig();
        ssoProvider.setResult(new DummySsoProvider());

        LiveNationApiBuilder apiBuilder = new LiveNationApiBuilder(clientId, deviceId, ssoProvider, appContext);

        Intent intent = new Intent();
        intent.putExtra(HomeActivity.NO_LIFECYCLE, true);
        setActivityIntent(intent);

        WeakReference<Activity> weakActivity = new WeakReference<Activity>(getActivity());
        apiBuilder.getActivity().setResult(weakActivity);

        apiBuilder.getAppContext().setResult(weakActivity.get().getApplicationContext());

        return apiBuilder;
    }
}

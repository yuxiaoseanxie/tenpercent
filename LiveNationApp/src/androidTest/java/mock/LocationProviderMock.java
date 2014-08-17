package mock;

import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

/**
 * Created by elodieferrais on 7/17/14.
 */
public class LocationProviderMock implements LocationProvider {
    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        Double[] loc = {37.7833d, 122.4167d};
        callback.onResponse(loc);
    }
}

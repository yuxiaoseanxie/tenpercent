package mock;

import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

/**
 * Created by elodieferrais on 7/17/14.
 */
public class LocationProviderMock implements LocationProvider {

    public static final Double[] MOCK_LOCATION = {45.9160d, 6.1330d};
    public Double[] location = MOCK_LOCATION;
    private boolean isSuccessFull = true;

    @Override
    public void getLocation(ProviderCallback<Double[]> callback) {
        if (isSuccessFull) {
            callback.onResponse(location);
        } else {
            callback.onErrorResponse();
        }
    }

    public void setSuccessFull(boolean isSuccessFull) {
        this.isSuccessFull = isSuccessFull;
    }

    public void setLocation(Double[] location) {
        this.location = location;
    }
}

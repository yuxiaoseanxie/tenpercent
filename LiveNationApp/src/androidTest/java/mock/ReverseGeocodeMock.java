package mock;

import android.content.Context;
import android.os.AsyncTask;

import com.livenation.mobile.android.na.providers.location.ReverseGeocode;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;

/**
 * Created by elodieferrais on 2/6/15.
 */
public class ReverseGeocodeMock extends ReverseGeocode {
    public static final String DEFAULT_NAME = "default name";

    public ReverseGeocodeMock(Context context, double lat, double lng, GetCityCallback callback) {
        super(context, lat, lng, callback);
    }


    @Override
    protected String doInBackground(Void... params) {
        return DEFAULT_NAME;
    }

    @Override
    protected void onPostExecute(String s) {
        callback.onGetCity(new City(DEFAULT_NAME, lat, lng));
    }
}

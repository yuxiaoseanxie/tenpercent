package com.livenation.mobile.android.na.analytics.services;

import android.content.Context;

import com.amplitude.api.Amplitude;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.Props;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cchilton on 1/12/15.
 */
public class AmplitudeAnalytics implements AnalyticService {

    public AmplitudeAnalytics(Context context) {
        Amplitude.initialize(context, context.getString(R.string.analytics_amplitude_apikey));
    }

    @Override
    public void screen(String title, Props props) {
        Amplitude.logEvent(title, getJsonProps(props));
    }

    @Override
    public void track(String event, Props props) {
        Amplitude.logEvent(event, getJsonProps(props));
    }

    private JSONObject getJsonProps(Props props) {
        JSONObject out = new JSONObject();
        for (String key : props.keySet()) {
            try {
                out.put(key, props.get(key));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return out;
    }
}

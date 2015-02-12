package analytics;

import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.analytics.services.AnalyticService;

/**
 * Created by cchilton on 1/13/15.
 */
public class MockAnalyticService implements AnalyticService {
    private final OnAnalytics listener;

    public MockAnalyticService(OnAnalytics listener) {
        this.listener = listener;
    }

    @Override
    public void screen(String screenTitle, Props props) {
        listener.onScreen(screenTitle, props);
    }

    @Override
    public void track(String event, Props props) {
        listener.onTrack(event, props);
    }

    public static interface OnAnalytics {
        void onScreen(String screenTitle, Props props);

        void onTrack(String event, Props props);
    }
}

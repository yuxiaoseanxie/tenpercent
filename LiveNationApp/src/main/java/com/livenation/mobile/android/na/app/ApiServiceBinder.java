package com.livenation.mobile.android.na.app;

import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;

/**
 * Created by cchilton on 3/7/14.
 */
public interface ApiServiceBinder {
    void onApiServiceAttached(LiveNationApiService apiService);
    void onApiServiceNotAvailable();
}

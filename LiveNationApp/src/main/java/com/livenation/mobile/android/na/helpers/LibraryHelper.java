package com.livenation.mobile.android.na.helpers;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService.SendLibraryAffinitiesCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;

/**
 * Created by elodieferrais on 3/27/14.
 */
public class LibraryHelper {

    public void sendLibraryScan(final LibraryDump libraryDump, final SendLibraryAffinitiesCallback callback) {
        if (!libraryDump.isEmpty()) {
            LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
                @Override
                public void onApiServiceAttached(LiveNationApiService apiService) {
                    apiService.sendLibraryAffinities(libraryDump, callback);
                }
            });
        }
    }
}

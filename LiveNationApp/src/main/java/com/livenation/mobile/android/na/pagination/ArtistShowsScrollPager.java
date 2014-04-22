package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ArtistEventsParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/21/14.
 */
public class ArtistShowsScrollPager extends BaseDecoratedScrollPager<Event, List<Event>> {

    public ArtistShowsScrollPager(ArrayAdapter<Event> adapter) {
        super(30, adapter);
    }

    @Override
    protected void fetch(LiveNationApiService apiService, int offset, int limit, ApiService.BasicApiCallback callback) {
        ArtistEventsParameters params = new ArtistEventsParameters();
        params.setPage(offset, limit);
        apiService.getArtistEvents(params, ArtistShowsScrollPager.this);
    }

    @Override
    public void onResponse(final List<Event> response) {
        //TODO remove that. Should be able to access location without binding the API
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                double lat = apiService.getApiConfig().getLat();
                double lng = apiService.getApiConfig().getLng();
                ArtistEvents artistEvents = ArtistEvents.from((ArrayList<Event>) response, lat, lng);
                callback.onResponse(artistEvents.getAll());
            }

            @Override
            public void onApiServiceNotAvailable() {
                //emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
            }
        });
    }
}


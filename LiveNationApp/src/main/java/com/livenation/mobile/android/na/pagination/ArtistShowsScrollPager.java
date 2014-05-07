package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ArtistEventsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/21/14.
 */
public class ArtistShowsScrollPager extends BaseDecoratedScrollPager<Event, List<Event>> {
    private final long artistId;

    public ArtistShowsScrollPager(long artistId, ArrayAdapter<Event> adapter) {
        super(30, adapter);
        this.artistId = artistId;
    }

    @Override
    protected void fetch(final LiveNationApiService apiService, int offset, int limit, final ApiService.BasicApiCallback callback) {
        ArtistEventsParameters params = new ArtistEventsParameters();
        params.setPage(offset, limit);
        params.setArtistId(artistId);

        apiService.getArtistEvents(params, new ApiService.BasicApiCallback<List<Event>>() {
            @Override
            public void onResponse(final List<Event> response) {
                LiveNationLibrary.getLocationProvider().getLocation(new ProviderCallback<Double[]>() {
                    @Override
                    public void onResponse(Double[] latlong) {
                        ArtistEvents artistEvents = ArtistEvents.from((ArrayList<Event>) response, latlong[0], latlong[1]);
                        callback.onResponse(artistEvents.getAll());
                    }

                    @Override
                    public void onErrorResponse() {}
                });

            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
    }


}


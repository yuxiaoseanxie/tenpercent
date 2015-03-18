package com.livenation.mobile.android.na.pagination;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.EventParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;

import java.util.ArrayList;
import java.util.List;

import android.mobile.livenation.com.livenationui.view.listener.BaseDecoratedScrollPager;
import android.widget.ArrayAdapter;

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
    protected void fetch(int offset, int limit, final BasicApiCallback callback) {
        EventParameters params = new EventParameters();
        params.setPage(offset, limit);

        LiveNationApplication.getLiveNationProxy().getArtistEvents(artistId, new BasicApiCallback<List<Event>>() {
            @Override
            public void onResponse(final List<Event> response) {
                LiveNationLibrary.getLocationProvider().getLocation(new ProviderCallback<Double[]>() {
                    @Override
                    public void onResponse(Double[] latlong) {
                        ArtistEvents artistEvents = ArtistEvents.from((ArrayList<Event>) response, latlong[0], latlong[1]);
                        callback.onResponse(artistEvents.getAll());
                    }

                    @Override
                    public void onErrorResponse() {
                        //TODO Define an empty view or "retry view"
                    }
                });

            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        }, params);
    }


}


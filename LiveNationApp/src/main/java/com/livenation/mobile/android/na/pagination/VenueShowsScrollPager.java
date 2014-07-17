package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.VenueEventsParameters;

import java.util.List;

/**
 * Created by Charlie on 4/21/14.
 */
public class VenueShowsScrollPager extends BaseDecoratedScrollPager<Event, List<Event>> {
    private final long venueId;

    public VenueShowsScrollPager(long venueId, ArrayAdapter<Event> adapter) {
        super(10, adapter);
        this.venueId = venueId;
    }

    @Override
    protected void fetch(final LiveNationApiService apiService, int offset, int limit, final ApiService.BasicApiCallback callback) {
        VenueEventsParameters params = new VenueEventsParameters();
        params.setPage(offset, limit);
        params.setVenueId(venueId);
        apiService.getVenueEvents(params, callback);
    }


}


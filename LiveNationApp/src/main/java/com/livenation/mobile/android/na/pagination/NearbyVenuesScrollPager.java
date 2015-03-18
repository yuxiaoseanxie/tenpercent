package com.livenation.mobile.android.na.pagination;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import android.mobile.livenation.com.livenationui.view.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.VenueParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

import android.mobile.livenation.com.livenationui.view.listener.BaseDecoratedScrollPager;
import android.widget.ArrayAdapter;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class NearbyVenuesScrollPager extends BaseDecoratedScrollPager<Event, List<Venue>> {

    /*
    We manually track the paging offset value as the adapter's itemCount() value will be incorrect
    for this purpose (since the size of venue events (adapter) != size of venues (api)
     */
    private int offset = 0;
    private final static int RADIUS = 50;

    public NearbyVenuesScrollPager(ArrayAdapter<Event> adapter) {
        super(DEFAULT_LIMIT, adapter);
    }

    @Override
    public void resetData() {
        super.resetData();
        offset = 0;
    }

    @Override
    protected int getOffset() {
        return offset;
    }

    @Override
    protected void fetch(int offset, int limit, final BasicApiCallback callback) {
        VenueParameters params = new VenueParameters();
        params.setMinimumNumberOfEvents(2);
        params.setIncludeEvents(true);
        params.setPage(offset, limit);
        params.setRadius(RADIUS);

        LiveNationApplication.getLiveNationProxy().getNearbyVenues(new BasicApiCallback<List<Venue>>() {
            @Override
            public void onResponse(List<Venue> response) {
                NearbyVenuesScrollPager.this.offset += response.size();
                List<Event> transformed = DataModelHelper.flattenVenueEvents(response);
                callback.onResponse(transformed);
                if (response.size() == 0 && emptyView != null) {
                    emptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
                }
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        }, params);
    }

}
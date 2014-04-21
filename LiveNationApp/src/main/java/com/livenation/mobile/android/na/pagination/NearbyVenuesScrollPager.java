package com.livenation.mobile.android.na.pagination;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.DataModelHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.NearbyVenuesWithEventsParameters;

import java.util.List;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class NearbyVenuesScrollPager extends BaseDecoratedScrollPager<Event> {

    /*
    We manually track the paging offset value as the adapter's itemCount() value will be incorrect
    for this purpose (since the size of venue events (adapter) != size of venues (api)
     */
    private int offset = 0;
    private EmptyListViewControl emptyView;

    public NearbyVenuesScrollPager(ArrayAdapter<Event> adapter) {
        super(10, adapter);
    }

    public NearbyVenuesScrollPager(ArrayAdapter<Event> adapter, EmptyListViewControl emptyView) {
        super(10, adapter);
        this.emptyView = emptyView;
    }

    @Override
    public void reset() {
        super.reset();
        offset = 0;
    }

    @Override
    protected int getOffset() {
        return offset;
    }

    @Override
    public void fetch(final int offset, final int limit, final ApiService.BasicApiCallback<List<Event>> callback) {
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                NearbyVenuesWithEventsParameters params = new NearbyVenuesWithEventsParameters();
                params.setMinimumNumberOfEvents(2);
                params.setPage(offset, limit);
                params.setLocation(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
                apiService.getNearbyVenuesWithEvents(params, new ApiService.BasicApiCallback<List<Venue>>() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("fail", "fail");
                        if (emptyView != null) {
                            emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                        }
                        callback.onErrorResponse(error);
                    }

                    @Override
                    public void onResponse(List<Venue> response) {
                        NearbyVenuesScrollPager.this.offset += response.size();
                        List<Event> transformed = DataModelHelper.flattenVenueEvents(response);
                        callback.onResponse(transformed);
                        if (response.size() == 0 && emptyView != null) {
                            emptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
                        }
                    }
                });
            }

            @Override
            public void onApiServiceNotAvailable() {
                if (emptyView != null) {
                    emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                }
            }
        });
    }
}
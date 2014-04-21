package com.livenation.mobile.android.na.pagination;

import android.util.Log;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.EventParameters;

import java.util.List;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class AllShowsScrollPager extends BaseDecoratedScrollPager<Event> {

    private EmptyListViewControl emptyView;

    public AllShowsScrollPager(ArrayAdapter<Event> adapter) {
        super(30, adapter);
    }

    public AllShowsScrollPager(ArrayAdapter<Event> adapter, EmptyListViewControl emptyView) {
        super(30, adapter);
        this.emptyView = emptyView;
    }

    @Override
    public void fetch(final int offset, final int limit, final ApiService.BasicApiCallback<List<Event>> callback) {
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                EventParameters params = new EventParameters();
                params.setPage(offset, limit);
                params.setLocation(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
                params.setSortMethod("start_time");
                apiService.getEvents(params, new ApiService.BasicApiCallback<List<Event>>() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("fail", "fail");
                        if (emptyView != null) {
                            emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                        }
                        callback.onErrorResponse(error);
                    }

                    @Override
                    public void onResponse(List<Event> response) {
                        callback.onResponse(response);
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
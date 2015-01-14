package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.BasedParameters;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.EventParameters;

import java.util.Calendar;
import java.util.List;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class AllShowsScrollPager extends BaseDecoratedScrollPager<Event, List<Event>> {

    public AllShowsScrollPager(ArrayAdapter<Event> adapter) {
        super(30, adapter);
    }

    @Override
    protected void fetch(int offset, int limit, BasicApiCallback callback) {
        EventParameters params = new EventParameters();
        params.setPage(offset, limit);
        params.setSortMethod(BasedParameters.SortField.StartTime);
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();
        end.add(Calendar.MONTH, 12);
        params.setTimeRange(start.getTimeInMillis(), end.getTimeInMillis());
        LiveNationApplication.getLiveNationProxy().getEvents(callback, params);
    }
}
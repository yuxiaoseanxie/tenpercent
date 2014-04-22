package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter.TaggedEvent;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.RecommendationSetsParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class RecommendationSetsScrollPager extends BaseDecoratedScrollPager<TaggedEvent, List<RecommendationSet>> {

    public RecommendationSetsScrollPager(ArrayAdapter<TaggedEvent> adapter) {
        super(DEFAULT_LIMIT, adapter);
    }

    @Override
    protected void fetch(LiveNationApiService apiService, final int offset, final int limit, ApiService.BasicApiCallback callback) {
        RecommendationSetsParameters params = new RecommendationSetsParameters();
        params.setPage(offset, limit);
        params.setLocation(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
        params.setIncludes(new String[]{"personal", "popular"});
        params.setRadius(Constants.DEFAULT_RADIUS);
        apiService.getRecommendationSets(params, RecommendationSetsScrollPager.this);
    }

    @Override
    public void onResponse(List<RecommendationSet> response) {
        List<TaggedEvent> result = new ArrayList<TaggedEvent>();
        for (RecommendationSet set : response) {
            boolean isPersonal = false;
            if ("personal".equalsIgnoreCase(set.getName())) {
                isPersonal = true;
            }
            for (Event event : set.getEvents()) {
                TaggedEvent taggedEvent = new TaggedEvent(event);
                taggedEvent.setTag(isPersonal);
                result.add(taggedEvent);
            }
        }
        callback.onResponse(result);

        if (result.size() == 0 && emptyView != null) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }
    }
}

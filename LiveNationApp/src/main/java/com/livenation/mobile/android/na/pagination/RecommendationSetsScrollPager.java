package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter.TaggedEvent;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.RecommendationSetsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class RecommendationSetsScrollPager extends BaseDecoratedScrollPager<TaggedEvent, List<RecommendationSet>> {
    private int pagingOffset = 0;

    //limit for initial "personal" recommendations request. 200 is the value that IOS uses when doing this same thing.
    private static final int PERSONAL_ONESHOT_LIMIT = 200;

    public RecommendationSetsScrollPager(ArrayAdapter<TaggedEvent> adapter) {
        super(DEFAULT_LIMIT, adapter);
    }

    @Override
    public void reset() {
        super.reset();
        pagingOffset = 0;
    }

    @Override
    protected void fetch(LiveNationApiService apiService, final int offset, final int limit, final ApiService.BasicApiCallback callback) {
        RecommendationSetsParameters params = new RecommendationSetsParameters();
        if (offset == 0 && pagingOffset == 0) {
            //if no data, do a one shot fetch for personal recommendation shows
            params.setIncludes(new String[]{"personal"});
            params.setPage(0, PERSONAL_ONESHOT_LIMIT);
        } else {
            //if data has already been loaded, page the next set of data
            params.setIncludes(new String[]{"popular"});
            params.setPage(pagingOffset, limit);
        }
        params.setLocation(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
        params.setRadius(Constants.DEFAULT_RADIUS);
        apiService.getRecommendationSets(params, new ApiService.BasicApiCallback<List<RecommendationSet>>() {
            @Override
            public void onResponse(List<RecommendationSet> response) {
                List<TaggedEvent> result = new ArrayList<TaggedEvent>();
                for (RecommendationSet set : response) {
                    boolean isPersonal = "personal".equalsIgnoreCase(set.getName());
                    if (!isPersonal) {
                        //if we have fetched popular shows, update our manually tracked paging offset
                        pagingOffset += set.getEvents().size();
                    }
                    for (Event event : set.getEvents()) {
                        TaggedEvent taggedEvent = new TaggedEvent(event);
                        taggedEvent.setTag(isPersonal);
                        result.add(taggedEvent);
                    }
                }
                callback.onResponse(result);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                callback.onErrorResponse(error);
            }
        });
    }
}

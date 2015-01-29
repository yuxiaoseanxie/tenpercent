package com.livenation.mobile.android.na.pagination;

import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.ui.adapters.RecommendationsAdapter.RecommendationItem;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet.RecommendationSetType;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.RecommendationSetsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/20/14.
 */
public class RecommendationSetsScrollPager extends BaseDecoratedScrollPager<RecommendationItem, List<RecommendationSet>> {
    //limit for initial "personal" recommendations request. 200 is the value that IOS uses when doing this same thing.
    private static final int PERSONAL_ONESHOT_LIMIT = 200;
    private static final int PERSONAL_RECOMMENDATIONS_LOW_UPSELL_THRESHOLD = 3;
    private int pagingOffset = 0;

    public RecommendationSetsScrollPager(ArrayAdapter<RecommendationItem> adapter) {
        super(DEFAULT_LIMIT, adapter);
    }

    @Override
    public void resetDataAndClearView() {
        super.resetDataAndClearView();
        pagingOffset = 0;
    }

    @Override
    public void onFetchEnded(boolean cancelled) {
        super.onFetchEnded(cancelled);
        if (!cancelled && pagingOffset == 0) {
            //if our manually tracked paging offset is 0, load another page.
            //This call fetches the first page of "popular" after the first page of "personal" has loaded
            load();
        }
    }

    @Override
    protected void fetch(final int offset, final int limit, final BasicApiCallback callback) {
        RecommendationSetsParameters params = new RecommendationSetsParameters();
        if (offset == 0 && pagingOffset == 0) {
            //if no data, do a one shot fetch for personal recommendation shows
            //this is one request to fetch all personal shows. This is the same technique IOS uses to load personal recommendations
            params.setIncludes(new String[]{"personal"});
            params.setPage(0, PERSONAL_ONESHOT_LIMIT);
        } else {
            //there is already data, default this fetch request to page through popular shows
            params.setIncludes(new String[]{"popular"});
            params.setPage(pagingOffset, limit);
        }
        params.setRadius(Constants.DEFAULT_RADIUS);
        LiveNationApplication.getLiveNationProxy().getRecommendationSets(params, new BasicApiCallback<List<RecommendationSet>>() {
            @Override
            public void onResponse(List<RecommendationSet> response) {
                List<RecommendationItem> result = new ArrayList<RecommendationItem>();

                for (RecommendationSet set : response) {
                    for (Event event : set.getEvents()) {
                        //add a personal or popular recommendation item
                        RecommendationItem item = createRecommendationItem(event, RecommendationSetType.getRecommendationSetType(set.getName()));
                        result.add(item);
                    }

                    final int eventCount = set.getEvents().size();
                    RecommendationSetType recommendationSetType = RecommendationSetType.getRecommendationSetType(set.getName());
                    switch (recommendationSetType) {
                        case PERSONAL:
                            if (set.getEvents().size() == 0) {
                                if (!LoginHelper.isUsingFacebook(getAdapter().getContext())) {
                                    //create a large "get some favs!" upsell to show if no personal recs
                                    RecommendationItem item = createSearchWithFavoriteUpsell();
                                    result.add(0, item);
                                } else {
                                    //create a medium "find some recs!" upsell to show if no personal recs
                                    //but user is already using facebook
                                    RecommendationItem item = createSearchFavoriteUpsell();
                                    result.add(0, item);
                                }
                            }
                            if ((eventCount > 0) && (eventCount <= PERSONAL_RECOMMENDATIONS_LOW_UPSELL_THRESHOLD)) {
                                //create a discreet in line upsell for adding favorites
                                RecommendationItem item = createDiscreetFavoriteUpsell();
                                result.add(item);
                            }
                            break;
                        default:
                            //non personal recs
                            //if we have fetched popular shows, update our manually tracked paging offset (which excludes personal items)
                            pagingOffset += eventCount;
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

    private RecommendationItem createRecommendationItem(Event event, RecommendationSetType recommendationType) {
        RecommendationItem item = new RecommendationItem(event);

        switch (recommendationType) {
            case PERSONAL:
                item.setTag(RecommendationItem.RecommendationType.EVENT_PERSONAL);
                break;
            case POPULAR:
                item.setTag(RecommendationItem.RecommendationType.EVENT_POPULAR);
                break;
        }

        return item;
    }

    private RecommendationItem createDiscreetFavoriteUpsell() {
        RecommendationItem item = new RecommendationItem();
        item.setTag(RecommendationItem.RecommendationType.FAVORITE_UPSELL_DISCREET);
        return item;
    }

    private RecommendationItem createSearchFavoriteUpsell() {
        RecommendationItem item = new RecommendationItem();
        item.setTag(RecommendationItem.RecommendationType.FAVORITE_UPSELL_SEARCH);
        return item;
    }

    private RecommendationItem createSearchWithFavoriteUpsell() {
        RecommendationItem item = new RecommendationItem();
        item.setTag(RecommendationItem.RecommendationType.FAVORITE_UPSELL_SEARCH_WITH_FACEBOOK);
        return item;
    }

}

package com.livenation.mobile.android.na.ui;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.ui.fragments.FavoriteSearchFragment;
import com.livenation.mobile.android.na.ui.fragments.SearchFragment;

/**
 * Created by cchilton on 4/2/14.
 */
public class FavoriteSearchActivity extends SearchActivity {
    public static final String EXTRA_KEY_SEARCH_MODE = "com.livenation.mobile.android.na.ui.SearchActivity.EXTRA_KEY_SEARCH_MODE";
    public static final String EXTRA_KEY_ON_CLICK_ACTION = "com.livenation.mobile.android.na.ui.SearchActivity.EXTRA_KEY_ON_CLICK_ACTION";
    public static final int EXTRA_VALUE_SEARCH_MODE_DEFAULT = 0;
    public static final int EXTRA_VALUE_SEARCH_MODE_ARTIST = 1;
    public static final int EXTRA_VALUE_SEARCH_MODE_ARTIST_VENUES = 2;
    public static final int EXTRA_VALUE_ON_CLICK_ACTION_OPEN = 0;
    public static final int EXTRA_VALUE_ON_CLICK_ACTION_FAVORITE = 1;


    @Override
    public SEARCH_MODE getSearchMode() {
        int searchModeExtra = FavoriteSearchActivity.EXTRA_VALUE_SEARCH_MODE_DEFAULT;
        if (getIntent() != null) {
            searchModeExtra = getIntent().getIntExtra(FavoriteSearchActivity.EXTRA_KEY_SEARCH_MODE, FavoriteSearchActivity.EXTRA_VALUE_SEARCH_MODE_DEFAULT);
        }
        switch (searchModeExtra) {
            case EXTRA_VALUE_SEARCH_MODE_ARTIST:
                return SEARCH_MODE.ARTISTS;
            case EXTRA_VALUE_SEARCH_MODE_ARTIST_VENUES:
                return SEARCH_MODE.ARTISTS_VENUES;
            default:
                return SEARCH_MODE.ARTISTS_VENUES_SHOWS;
        }
    }

    @Override
    protected SearchFragment getFragmentInstance() {
        FavoriteSearchFragment searchFragment = new FavoriteSearchFragment();
        searchFragment.setSearchMode(getSearchMode());
        searchFragment.setClickMode(getClickMode());
        return searchFragment;
    }

    private FavoriteSearchFragment.CLICK_MODE getClickMode() {
        int clickModeExtra = FavoriteSearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_OPEN;
        if (getIntent() != null) {
            clickModeExtra = getIntent().getIntExtra(FavoriteSearchActivity.EXTRA_KEY_ON_CLICK_ACTION, FavoriteSearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_OPEN);
        }
        switch (clickModeExtra) {
            case FavoriteSearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_FAVORITE:
                return FavoriteSearchFragment.CLICK_MODE.FAVORITE;
            default:
                return FavoriteSearchFragment.CLICK_MODE.OPEN_DETAIL;
        }
    }

    @Override
    protected String getScreenName() {
        return AnalyticConstants.SCREEN_SEARCH;
    }

    @Override
    protected String getOmnitureScreenName() {
        return AnalyticConstants.OMNITURE_SCREEN_SEARCH;
    }
}

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.SearchActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.adapters.SearchAdapter;
import com.livenation.mobile.android.na.ui.viewcontroller.SearchViewHolder;
import com.livenation.mobile.android.na.utils.EventUtils;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.SearchResult;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.AutoCompleteSearchParameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;


import static com.livenation.mobile.android.na.ui.fragments.FavoriteSearchFragment.CLICK_MODE.OPEN_DETAIL;

/**
 * Created by cchilton on 4/2/14.
 */
public class FavoriteSearchFragment extends SearchFragment<SearchResult> {
    private final String[] SEARCH_INCLUDE_ARTISTS_VENUES_SHOWS = new String[]{"venues", "artists", "events"};
    private final String[] SEARCH_INCLUDE_ARTISTS_VENUES = new String[]{"venues", "artists"};
    private final String[] SEARCH_INCLUDE_ARTISTS = new String[]{"artists"};
    private String[] searchIncludes = SEARCH_INCLUDE_ARTISTS_VENUES_SHOWS;
    private CLICK_MODE clickMode = OPEN_DETAIL;

    public void setSearchMode(SearchActivity.SEARCH_MODE searchMode) {
        switch (searchMode) {
            case ARTISTS:
                searchIncludes = SEARCH_INCLUDE_ARTISTS;
                break;
            case ARTISTS_VENUES:
                searchIncludes = SEARCH_INCLUDE_ARTISTS_VENUES;
                break;
            case ARTISTS_VENUES_SHOWS:
            default:
                searchIncludes = SEARCH_INCLUDE_ARTISTS_VENUES_SHOWS;
                break;
        }
    }

    public void setClickMode(CLICK_MODE clickMode) {
        this.clickMode = clickMode;
    }


    @Override
    public void searchFor(String text, BasicApiCallback<List<SearchResult>> callback) {
        AutoCompleteSearchParameters params = new AutoCompleteSearchParameters();
        params.setIncludes(searchIncludes);
        params.setSearchQuery(text);
        LiveNationApplication.getLiveNationProxy().autoCompleteSearch(params, callback);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        switch (clickMode) {
            case FAVORITE:
                favoriteItemClick(view);
                break;
            default:
                openSearchItem(position);
                break;
        }
    }


    private void openSearchItem(int position) {
        SearchResult searchResult = adapter.getItem(position);
        if (searchResult.getNumericalId() == null) {
            return;
        }
        Props props = new Props();
        switch (searchResult.getSearchResultType()) {
            case Venue: {
                props.put(AnalyticConstants.VENUE_NAME, searchResult.getName());
                props.put(AnalyticConstants.VENUE_ID, searchResult.getNumericalId());

                Intent intent = new Intent(getActivity(), VenueActivity.class);
                String entityId = Venue.getAlphanumericId(searchResult.getNumericalId());
                Bundle args = VenueActivity.getArguments(entityId);
                intent.putExtras(args);
                startActivity(intent);
                break;
            }

            case Artist: {
                props.put(AnalyticConstants.ARTIST_NAME, searchResult.getName());
                props.put(AnalyticConstants.ARTIST_ID, searchResult.getNumericalId());

                Intent intent = new Intent(getActivity(), ArtistActivity.class);
                String entityId = Artist.getAlphanumericId(searchResult.getNumericalId());
                Bundle args = ArtistActivity.getArguments(entityId);
                intent.putExtras(args);
                startActivity(intent);
                break;
            }

            case Event: {
                props.put(AnalyticConstants.EVENT_NAME, searchResult.getName());
                props.put(AnalyticConstants.EVENT_ID, searchResult.getNumericalId());

                EventUtils.redirectToSDP(getActivity(), searchResult.getNumericalId().toString());
                break;
            }
        }
        LiveNationAnalytics.track(AnalyticConstants.SEARCH_RESULT_TAP, AnalyticsCategory.SEARCH, props);
    }

    private void favoriteItemClick(View view) {
        View checkbox = view.findViewById(R.id.list_search_checkbox);
        checkbox.performClick();
    }

    @Override
    public SearchAdapter<SearchResult> getAdapter() {
        return new FavoriteSearchAdapter(getActivity(), new ArrayList<SearchResult>());
    }

    public class FavoriteSearchAdapter extends SearchAdapter<SearchResult> {

        public FavoriteSearchAdapter(Context context, List<SearchResult> items) {
            super(context, items);
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            SearchViewHolder holder = (SearchViewHolder) view.getTag();
            SearchResult searchResult = getItem(position);
            holder.title.setText(searchResult.getName());
            holder.type.setText(searchResult.getObjectType().toLowerCase(Locale.getDefault()));
            holder.checkBox.setVisibility(View.VISIBLE);

            Favorite favorite = new Favorite();
            favorite.setName(searchResult.getName());
            favorite.setId(searchResult.getNumericalId());
            switch (searchResult.getSearchResultType()) {
                case Artist: {
                    favorite.setIntType(Favorite.FAVORITE_ARTIST);
                    holder.checkBox.bindToFavorite(favorite, AnalyticsCategory.SEARCH);
                    break;
                }
                case Venue: {
                    favorite.setIntType(Favorite.FAVORITE_VENUE);
                    holder.checkBox.bindToFavorite(favorite, AnalyticsCategory.SEARCH);
                    break;
                }
                default:
                    holder.checkBox.setVisibility(View.INVISIBLE);
            }

            return view;
        }

        @Override
        protected int getLayoutCellId() {
            return R.layout.list_search_result;
        }

    }

    public enum CLICK_MODE {
        FAVORITE,
        OPEN_DETAIL
    }
}

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SearchForText;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.SearchActivity;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.SearchResult;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.AutoCompleteSearchParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.segment.android.models.Props;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchFragment extends LiveNationFragment implements SearchForText, BasicApiCallback<List<SearchResult>>, ListView.OnItemClickListener {
    private final String[] SEARCH_INCLUDE_DEFAULT = new String[]{"venues", "artists", "events"};
    private String[] searchIncludes = SEARCH_INCLUDE_DEFAULT;
    private final String[] SEARCH_INCLUDE_ARTISTS_VENUES = new String[]{"venues", "artists"};
    private final String[] SEARCH_INCLUDE_ARTISTS = new String[]{"artists"};
    private SearchAdapter adapter;
    private LiveNationApiService apiService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new SearchAdapter(getActivity(), new ArrayList<SearchResult>());
        SearchActivity searchActivity = (SearchActivity) getActivity();
        switch (searchActivity.getSearchMode()) {
            case SearchActivity.EXTRA_VALUE_SEARCH_MODE_ARTIST:
                searchIncludes = SEARCH_INCLUDE_ARTISTS;
                break;
            case SearchActivity.EXTRA_VALUE_SEARCH_MODE_ARTIST_VENUES:
                searchIncludes = SEARCH_INCLUDE_ARTISTS_VENUES;
                break;
            default:
                searchIncludes = SEARCH_INCLUDE_DEFAULT;
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void searchFor(String text) {
        if (TextUtils.isEmpty(text)) return;
        AutoCompleteSearchParameters params = new AutoCompleteSearchParameters();
        params.setIncludes(searchIncludes);
        params.setSearchQuery(text);
        LiveNationApplication.getLiveNationProxy().autoCompleteSearch(params, this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        switch (((SearchActivity) getActivity()).getOnClickActionMode()) {
            case SearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_OPEN:
                openSearchItem(position);
                break;
            case SearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_FAVORITE:
                favoriteItemClick(view);
                break;
        }
    }

    @Override
    public void onErrorResponse(LiveNationError error) {
    }

    @Override
    public void onResponse(List<SearchResult> response) {
        adapter.clear();
        for (SearchResult searchResult : response) {
            adapter.add(searchResult);
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
                Bundle args = SingleVenuePresenter.getAruguments(entityId);
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

                Intent intent = new Intent(getActivity(), ShowActivity.class);
                String entityId = Event.makeTypedId(searchResult.getNumericalId().toString());
                Bundle args = ShowActivity.getArguments(entityId);
                intent.putExtras(args);
                startActivity(intent);
                break;
            }
        }
        LiveNationAnalytics.track(AnalyticConstants.SEARCH_RESULT_TAP, AnalyticsCategory.SEARCH, props);
    }

    private void favoriteItemClick(View view) {
        View checkbox = view.findViewById(R.id.list_search_result_checkbox);
        checkbox.performClick();
    }

    public class SearchAdapter extends ArrayAdapter<SearchResult> {
        private LayoutInflater inflater;

        public SearchAdapter(Context context, List<SearchResult> items) {
            super(context, android.R.layout.simple_list_item_1, items);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;

            if (null == convertView) {
                view = inflater.inflate(R.layout.list_search_result, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            SearchResult searchResult = getItem(position);
            holder.getTitle().setText(searchResult.getName());
            holder.getType().setText(searchResult.getObjectType().toLowerCase());

            holder.getCheckBox().setVisibility(View.VISIBLE);
            Favorite favorite = new Favorite();
            favorite.setName(searchResult.getName());
            favorite.setId(searchResult.getNumericalId());
            switch (searchResult.getSearchResultType()) {
                case Artist: {
                    favorite.setIntType(Favorite.FAVORITE_ARTIST);
                    holder.getCheckBox().bindToFavorite(favorite, AnalyticsCategory.SEARCH);
                    break;
                }
                case Venue: {
                    favorite.setIntType(Favorite.FAVORITE_VENUE);
                    holder.getCheckBox().bindToFavorite(favorite, AnalyticsCategory.SEARCH);
                    break;
                }
                default:
                    holder.getCheckBox().setVisibility(View.INVISIBLE);
            }

            return view;
        }


        private class ViewHolder {
            private final TextView type;
            private final TextView title;
            private final FavoriteCheckBox checkBox;

            public ViewHolder(View view) {
                this.type = (TextView) view.findViewById(R.id.list_search_result_type_box);
                this.title = (TextView) view.findViewById(R.id.list_search_result_text);
                this.checkBox = (FavoriteCheckBox) view.findViewById(R.id.list_search_result_checkbox);
            }

            public TextView getType() {
                return type;
            }

            public TextView getTitle() {
                return title;
            }

            public FavoriteCheckBox getCheckBox() {
                return checkBox;
            }
        }

    }
}

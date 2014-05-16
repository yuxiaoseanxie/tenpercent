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
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SearchForText;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleVenuePresenter;
import com.livenation.mobile.android.na.ui.ArtistActivity;
import com.livenation.mobile.android.na.ui.SearchActivity;
import com.livenation.mobile.android.na.ui.VenueActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.SearchResult;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.AutoCompleteSearchParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchFragment extends LiveNationFragment implements SearchForText, ApiServiceBinder, ApiService.BasicApiCallback<List<SearchResult>>, ListView.OnItemClickListener {
    private final String[] SEARCH_INCLUDE_VENUES_ARTISTS = new String[]{"venues", "artists"};
    private final String[] SEARCH_INCLUDE_ARTISTS = new String[]{"artists"};
    private String[] searchIncludes = SEARCH_INCLUDE_VENUES_ARTISTS;

    private SearchAdapter adapter;
    private LiveNationApiService apiService;
    private String unboundSearchTextBuffer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new SearchAdapter(getActivity(), new ArrayList<SearchResult>());
        LiveNationApplication.get().getConfigManager().bindApi(this);

        if (getActivity().getIntent() != null) {
            int searchMode = getActivity().getIntent().getIntExtra(SearchActivity.SEARCH_MODE_KEY, SearchActivity.SEARCH_MODE_DEFAULT_VALUE);
            switch (searchMode) {
                case SearchActivity.SEARCH_MODE_ARTIST_ONLY_VALUE:
                    searchIncludes = SEARCH_INCLUDE_ARTISTS;
                    break;
                default:
                    searchIncludes = SEARCH_INCLUDE_VENUES_ARTISTS;
                    break;
            }
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
        if (null == apiService) {
            unboundSearchTextBuffer = text;
            return;
        }
        AutoCompleteSearchParameters params = new AutoCompleteSearchParameters();
        params.setIncludes(searchIncludes);
        params.setSearchQuery(text);

        apiService.autoCompleteSearch(params, this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        this.apiService = apiService;
        if (null != unboundSearchTextBuffer) {
            searchFor(unboundSearchTextBuffer);
            unboundSearchTextBuffer = null;
        }
    }

    @Override
    public void onApiServiceNotAvailable() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        SearchResult searchResult = adapter.getItem(position);

        switch (searchResult.getSearchResultType()) {
            case Venue: {
                Intent intent = new Intent(getActivity(), VenueActivity.class);
                String entityId = Venue.getAlphanumericId(searchResult.getLnid());
                Bundle args = SingleVenuePresenter.getAruguments(entityId);
                intent.putExtras(args);
                startActivity(intent);
                break;
            }

            case Artist: {
                Intent intent = new Intent(getActivity(), ArtistActivity.class);
                String entityId = Artist.getAlphanumericId(searchResult.getLnid());
                Bundle args = SingleArtistPresenter.getAruguments(entityId);
                intent.putExtras(args);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public void onErrorResponse(LiveNationError error) {
    }

    @Override
    public void onResponse(List<SearchResult> response) {
        adapter.clear();
        adapter.addAll(response);
        adapter.notifyDataSetChanged();
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
            switch (searchResult.getSearchResultType()) {
                case Artist: {
                    int favoriteTypeId = Favorite.FAVORITE_ARTIST;
                    holder.getCheckBox().bindToFavorite(favoriteTypeId, searchResult.getName(), searchResult.getLnid(), getFavoritesPresenter());
                    break;
                }
                case Venue: {
                    int favoriteTypeId = Favorite.FAVORITE_VENUE;
                    holder.getCheckBox().bindToFavorite(favoriteTypeId, searchResult.getName(), searchResult.getLnid(), getFavoritesPresenter());
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

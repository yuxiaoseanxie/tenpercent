package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SearchForText;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService.AutoCompleteSearchCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.SearchResult;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 4/2/14.
 */
public class SearchFragment extends LiveNationFragment implements SearchForText, ApiServiceBinder, AutoCompleteSearchCallback {
    private SearchAdapter adapter;
    private LiveNationApiService apiService;
    private final String[] SEARCH_INCLUDES = new String[] {"venues", "artists"};
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new SearchAdapter(getActivity(), new ArrayList<SearchResult>());
        LiveNationApplication.get().getApiHelper().bindApi(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        return view;
    }

    @Override
    public void searchFor(String text) {
        if (TextUtils.isEmpty(text)) return;

        ApiParameters.AutoCompleteSearchParameters params = ApiParameters.createAutoCompleteSearchParameters();
        params.setIncludes(SEARCH_INCLUDES);
        params.setSearchQuery(text);

        apiService.autoCompleteSearch(params, this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void onSuccess(List<SearchResult> results) {
        adapter.clear();
        adapter.addAll(results);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onFailure(int errorCode, String message) {}

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

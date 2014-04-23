package com.livenation.mobile.android.na.ui.fragments;

import android.app.Activity;
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
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SearchCitiesParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 4/2/14.
 */
public class CitySearchFragment extends LiveNationFragment implements SearchForText, ApiServiceBinder, ApiService.BasicApiCallback<List<City>>, ListView.OnItemClickListener {
    private SearchAdapter adapter;
    private LiveNationApiService apiService;
    private String unboundSearchTextBuffer;
    public static final String DATA_RESULT_KEY = "search_result";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = new SearchAdapter(getActivity(), new ArrayList<City>());
        LiveNationApplication.get().getApiHelper().bindApi(this);
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

    public void searchFor(String text) {
        if (TextUtils.isEmpty(text)) return;
        if (null == apiService) {
            unboundSearchTextBuffer = text;
            return;
        }
        SearchCitiesParameters params = new SearchCitiesParameters();
        params.setSearchQuery(text);

        apiService.searchCities(params, this);
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
    public void onErrorResponse(LiveNationError error) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        City city = adapter.getItem(position);
        Intent data = new Intent();
        data.putExtra(DATA_RESULT_KEY, city);
        getActivity().setResult(Activity.RESULT_OK, data);
        getActivity().finish();
    }

    @Override
    public void onResponse(List<City> response) {
        adapter.clear();
        adapter.addAll(response);
        adapter.notifyDataSetChanged();
    }

    public class SearchAdapter extends ArrayAdapter<City> {
        private LayoutInflater inflater;

        public SearchAdapter(Context context, List<City> items) {
            super(context, android.R.layout.simple_list_item_1, items);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;

            if (null == convertView) {
                view = inflater.inflate(R.layout.list_search_city_item, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            City city = getItem(position);
            holder.getTitle().setText(city.getName());

            return view;
        }

        private class ViewHolder {
            private final TextView title;

            public ViewHolder(View view) {
                this.title = (TextView) view.findViewById(R.id.list_search_city_title);
            }

            public TextView getTitle() {
                return title;
            }
        }

    }
}

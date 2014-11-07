package com.livenation.mobile.android.na.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.adapters.SearchAdapter;
import com.livenation.mobile.android.na.ui.viewcontroller.SearchViewHolder;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.SearchCitiesParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 4/2/14.
 */
public class CitySearchFragment extends SearchFragment<City> {
    public static final String DATA_RESULT_KEY = "search_result";


    @Override
    public void searchFor(String text, BasicApiCallback<List<City>> callback) {
        SearchCitiesParameters params = new SearchCitiesParameters();
        params.setSearchQuery(text);
        LiveNationApplication.getLiveNationProxy().searchCities(params, callback);

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
    public SearchAdapter<City> getAdapter() {
        return new CitySearchAdapter(getActivity(), new ArrayList<City>());
    }


    public class CitySearchAdapter extends SearchAdapter<City> {

        public CitySearchAdapter(Context context, List<City> items) {
            super(context, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            SearchViewHolder holder = (SearchViewHolder) view.getTag();

            City city = getItem(position);
            holder.title.setText(city.getName());

            return view;
        }

        @Override
        protected int getLayoutCellId() {
            return R.layout.list_search_city_item;
        }
    }
}

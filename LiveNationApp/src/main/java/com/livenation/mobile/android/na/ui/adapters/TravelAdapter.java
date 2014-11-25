package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.ui.dialogs.TravelListPopupWindow.TravelOption;

/**
 * Created by elodieferrais on 11/25/14.
 */
public class TravelAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    public TravelAdapter(@NonNull Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return TravelOption.values().length;
    }

    @Override
    public Object getItem(int position) {
        return TravelOption.values()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TravelOption option = TravelOption.values()[position];
        if (convertView != null) return convertView;
        switch (option) {
            case uber:
                View root = inflater.inflate(R.layout.popup_list_uber, parent, false);
                View firstRide = root.findViewById(android.R.id.text2);

                if (AnalyticsHelper.isAppInstalled(ExternalApplicationAnalytics.UBER.getPackageName(), context)) {
                    //hide "get your first ride free!" text
                    firstRide.setVisibility(View.GONE);
                }

                return root;
            case maps:
                return inflater.inflate(R.layout.popup_list_maps, parent, false);
        }
        throw new IllegalArgumentException();
    }
}

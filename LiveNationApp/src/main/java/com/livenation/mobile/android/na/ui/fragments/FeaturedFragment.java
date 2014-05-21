/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class FeaturedFragment extends LiveNationFragment implements FeatureView, ApiServiceBinder {
    private ViewGroup chartingContainer;
    private List<Chart> featured;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        featured = new ArrayList<Chart>();
        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_featured, container, false);
        chartingContainer = (ViewGroup) result.findViewById(R.id.featured_charting_container);
        setFeatured(featured);
        return result;
    }

    @Override
    public void setFeatured(List<Chart> featured) {
        this.featured = featured;
        if (null != chartingContainer) {
            setFeaturedView(featured);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveNationApplication.get().getConfigManager().persistentUnbindApi(this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        getFeaturePresenter().initialize(getActivity(), null, this);
    }

    @Override
    public void onApiServiceNotAvailable() {

    }

    private void setFeaturedView(List<Chart> featured) {
        final int spacerWidth = getActivity().getResources().getDimensionPixelSize(R.dimen.view_featured_item_spacer);
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        chartingContainer.removeAllViews();
        for (Chart chart : featured) {
            if (TextUtils.isEmpty(chart.getImageUrl())) continue;

            View view = inflater.inflate(R.layout.view_featured_item, chartingContainer, false);

            NetworkImageView image = (NetworkImageView) view.findViewById(android.R.id.icon);

            image.setImageUrl(chart.getImageUrl(), getImageLoader());

            TextView text = (TextView) view.findViewById(android.R.id.text1);
            text.setText(chart.getArtistName());

            chartingContainer.addView(view);

            View spacer = new View(getActivity());
            chartingContainer.addView(spacer, new LayoutParams(spacerWidth, LayoutParams.MATCH_PARENT));
        }
    }
}

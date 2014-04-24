/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;

import java.util.ArrayList;
import java.util.List;

public class FeaturedFragment extends LiveNationFragment implements FeatureView, ApiServiceBinder {
    private ViewGroup chartingContainer;
    private List<Chart> featured;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        featured = new ArrayList<Chart>();
        LiveNationApplication.get().getApiHelper().persistentBindApi(this);
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
        LiveNationApplication.get().getApiHelper().persistentUnbindApi(this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        getFeaturePresenter().initialize(getActivity(), null, this);
    }

    @Override
    public void onApiServiceNotAvailable() {

    }

    private void setFeaturedView(List<Chart> featured) {
        chartingContainer.removeAllViews();

        if (featured == null || featured.size() < 3) {
            //TODO: Investigate the default behavior for when there's nothing featured
            //throw new IllegalStateException("Returned featured data is missing/too short");
            return;
        }

        for (int i = 0; i < 3; i++) {
            Chart chart = featured.get(i);
            //TODO: Valid image url helper here
            if (null == chart.getImageUrl() ||
                    chart.getImageUrl().trim().length() == 0) {
                continue;
            }

            NetworkImageView image = new NetworkImageView(getActivity());
            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            image.setImageUrl(chart.getImageUrl(), getImageLoader());

            chartingContainer.addView(image, layoutParams);
        }
    }
}

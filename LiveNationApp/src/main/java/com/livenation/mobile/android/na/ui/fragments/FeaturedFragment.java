/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.FeatureView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;

public class FeaturedFragment extends LiveNationFragment implements FeatureView {
	private ViewGroup chartingContainer;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_featured, container, false);
		chartingContainer = (ViewGroup) result.findViewById(R.id.featured_charting_container);
		return result;
	}

	@Override
	public void setFeatured(List<Chart> featured) {
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
			LayoutParams layoutParams =  new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			image.setImageUrl(chart.getImageUrl(), getImageLoader());
			
			chartingContainer.addView(image, layoutParams);
		}
	}
}

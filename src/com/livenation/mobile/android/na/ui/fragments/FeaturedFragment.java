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

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationHelper;
import com.livenation.mobile.android.na.ui.fragments.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Chart;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.ApiParameters;

public class FeaturedFragment extends LiveNationFragment {
	private ViewGroup chartingContainer;
	private LocationCallback locationCallback = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (locationCallback == null) {
			locationCallback = new LocationCallback();
			getLocationHelper().getLocation(getActivity(), locationCallback);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.fragment_featured, container, false);
		chartingContainer = (ViewGroup) result.findViewById(R.id.featured_charting_container);
		return result;
	}

	private void getData(double lat, double lng) {
		ApiParameters.TopChartParameters parameters = ApiParameters.createChartParameters();
		parameters.setLocation(lat, lng);
		getApiService().getTopCharts(parameters, new OnGetTopCharts());
	}



	private class LocationCallback implements LocationHelper.LocationCallback {

		@Override
		public void onLocation(double lat, double lng) {
			getData(lat, lng);
		}

		@Override
		public void onLocationFailure(int failureCode) {
			//TODO: this
		}
	};

	private class OnGetTopCharts implements
			LiveNationApiService.GetTopChartsCallback {

		@Override
		public void onGetCharts(List<Chart> topCharts) {
			for (Chart chart : topCharts) {
				//TODO: DataModel helper method here
				if (null == chart.getImageUrl() || 
						chart.getImageUrl().trim().length() == 0) {
					continue;
				}
				NetworkImageView image = new NetworkImageView(getActivity());
				image.setImageUrl(chart.getImageUrl(), getImageLoader());
				chartingContainer.addView(image, new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
		}

		@Override
		public void onFailure(int errorCode, String message) {
			// TODO Auto-generated method stub

		}

	}

	private ImageLoader getImageLoader() {
		return LiveNationApplication.get().getImageLoader();
	}
}

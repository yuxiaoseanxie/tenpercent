/*
 * 
 * @author Charlie Chilton 2014/02/04
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.ui.FavoriteActivity;
import com.livenation.mobile.android.na.ui.LocationActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.ticketing.Ticketing;

public class AccountFragment extends LiveNationFragment implements LocationManager.GetCityCallback, ApiServiceBinder {
    private TextView locationText;
    private final String PROFILE_FRAGMENT_TAG = "profile_fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_account, container,
                false);

        OnOrdersClick ordersClick = new OnOrdersClick();
        result.findViewById(R.id.account_detail_order_history_container).setOnClickListener(ordersClick);

        OnFavoriteClick favoritesOnClick = new OnFavoriteClick();
        result.findViewById(R.id.account_detail_favorites_container).setOnClickListener(favoritesOnClick);

        View locationContainer = result.findViewById(R.id.fragment_account_footer);
        locationContainer.setOnClickListener(new OnLocationClick());

        locationText = (TextView) result.findViewById(R.id.account_footer_location_detail);

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        LiveNationApplication.get().getConfigManager().persistentBindApi(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        LiveNationApplication.get().getConfigManager().persistentUnbindApi(this);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        refreshUser(LoginHelper.isLogout());
        getLocationManager().reverseGeocodeCity(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng(), getActivity(), this);
    }

    @Override
    public void onApiServiceNotAvailable() {

    }

    public void refreshUser(boolean isLogout) {
        Fragment profileFragment = getChildFragmentManager().findFragmentByTag(PROFILE_FRAGMENT_TAG);
        if (null != profileFragment) {
            removeFragment(profileFragment);
        }

        if (isLogout) {
            profileFragment = new AccountSignInFragment();
        } else {
            profileFragment = new AccountUserFragment();
        }

        addFragment(R.id.account_header_provider_container, profileFragment, PROFILE_FRAGMENT_TAG);
    }

    @Override
    public void onGetCity(City city) {
        locationText.setText(city.getName());
    }

    @Override
    public void onGetCityFailure(double lat, double lng) {
        Context context = locationText.getContext();
        locationText.setText(context.getString(R.string.location_unknown) + " " + String.valueOf(lat)  + "," + String.valueOf(lng));
    }

    private class OnOrdersClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            LiveNationAnalytics.track(AnalyticConstants.YOUR_ORDERS_TAP, AnalyticsCategory.DRAWER);
            Ticketing.showOrderHistory(getActivity());
        }
    }

    private class OnFavoriteClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            LiveNationAnalytics.track(AnalyticConstants.YOUR_FAVORITES_TAP, AnalyticsCategory.DRAWER);
            Intent intent = new Intent(getActivity(), FavoriteActivity.class);
            startActivity(intent);
        }
    }

    private class OnLocationClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            LiveNationAnalytics.track(AnalyticConstants.YOUR_LOCATION_TAP, AnalyticsCategory.DRAWER);
            Intent intent = new Intent(getActivity(), LocationActivity.class);
            startActivity(intent);
        }
    }
}

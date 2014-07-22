/*
 * 
 * @author Charlie Chilton 2014/02/04
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.providers.location.LocationManager;
import com.livenation.mobile.android.na.ui.FavoriteActivity;
import com.livenation.mobile.android.na.ui.LocationActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.proxy.LiveNationConfig;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;
import com.livenation.mobile.android.platform.api.proxy.ProviderManager;
import com.livenation.mobile.android.ticketing.Ticketing;

public class AccountFragment extends LiveNationFragment implements LocationManager.GetCityCallback, ConfigCallback, LocationUpdateReceiver.LocationUpdateListener {
    private final String PROFILE_FRAGMENT_TAG = "profile_fragment";
    private Fragment profileFragment;
    private TextView locationText;
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);
    private BroadcastReceiver loginLogoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshUser(LoginHelper.isLogout());
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_account, container, false);

        //Views and click listeners
        OnOrdersClick ordersClick = new OnOrdersClick();
        result.findViewById(R.id.account_detail_order_history_container).setOnClickListener(ordersClick);

        OnFavoriteClick favoritesOnClick = new OnFavoriteClick();
        result.findViewById(R.id.account_detail_favorites_container).setOnClickListener(favoritesOnClick);

        View locationContainer = result.findViewById(R.id.fragment_account_footer);
        locationContainer.setOnClickListener(new OnLocationClick());

        locationText = (TextView) result.findViewById(R.id.account_footer_location_detail);

        //Get location for update the screen
        LiveNationApplication.getProviderManager().getConfigReadyFor(this, ProviderManager.ProviderType.LOCATION);

        //Register for being notify when the location change
        registerBroadcastReceiverForUpdate();

        return result;
    }

    private void registerBroadcastReceiverForUpdate() {
        Context context = LiveNationApplication.get().getApplicationContext();
        LocalBroadcastManager.getInstance(context).registerReceiver(locationUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));
        LocalBroadcastManager.getInstance(context).registerReceiver(loginLogoutReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGIN_INTENT_FILTER));
        LocalBroadcastManager.getInstance(context).registerReceiver(loginLogoutReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGOUT_INTENT_FILTER));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Context context = LiveNationApplication.get().getApplicationContext();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(loginLogoutReceiver);
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
        locationText.setText(getString(R.string.location_unknown) + " " + String.valueOf(lat) + "," + String.valueOf(lng));
    }

    //Get location for display data
    @Override
    public void onResponse(LiveNationConfig response) {
        refreshUser(LoginHelper.isLogout());
        LiveNationApplication.getLocationProvider().reverseGeocodeCity(response.getLat(), response.getLng(), getActivity(), this);
    }

    @Override
    public void onErrorResponse(int errorCode) {
    }

    //Location update

    @Override
    public void onLocationUpdated(int mode, double lat, double lng) {
        LiveNationApplication.getLocationProvider().reverseGeocodeCity(lat, lng, getActivity(), this);
    }

    //Click listener

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

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
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.presenters.views.AccountUserView;
import com.livenation.mobile.android.na.receiver.LocationUpdateReceiver;
import com.livenation.mobile.android.na.ui.FavoriteActivity;
import com.livenation.mobile.android.na.ui.LocationActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.ProviderManager;
import com.livenation.mobile.android.platform.init.proxy.LiveNationConfig;
import com.livenation.mobile.android.platform.util.Logger;
import com.livenation.mobile.android.ticketing.Ticketing;

public class AccountFragment extends LiveNationFragment implements AccountUserView, LocationManager.GetCityCallback, ConfigCallback, LocationUpdateReceiver.LocationUpdateListener {
    private Fragment profileFragment;
    private TextView locationText;
    private LocationUpdateReceiver locationUpdateReceiver = new LocationUpdateReceiver(this);

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

        OnFavoriteClick favoriteArtistOnClick = new OnFavoriteClick(FavoritesFragment.ARG_VALUE_ARTISTS);
        result.findViewById(R.id.account_detail_favorite_artists_container).setOnClickListener(favoriteArtistOnClick);

        OnFavoriteClick favoriteVenueOnClick = new OnFavoriteClick(FavoritesFragment.ARG_VALUE_VENUES);
        result.findViewById(R.id.account_detail_favorite_venues_container).setOnClickListener(favoriteVenueOnClick);

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
        LocalBroadcastManager.getInstance(context).registerReceiver(locationUpdateReceiver, new IntentFilter(Constants.Receiver.LOCATION_UPDATE_INTENT_FILTER));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Context context = LiveNationApplication.get().getApplicationContext();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver);
    }

    @Override
    public void setUser(User user) {
        if (null != profileFragment) {
            removeFragment(profileFragment);
            profileFragment = null;
        }

        if (null == user) {
            profileFragment = new AccountSignInFragment();
        } else {
            profileFragment = new AccountUserFragment();
        }

        addFragment(R.id.account_header_provider_container, profileFragment, "account_provider");
    }

    @Override
    public void onGetCity(String city) {
        locationText.setText(city);
    }

    @Override
    public void onGetCityFailure() {
        locationText.setText("Geocode failed!");
    }

    //Get location for display data

    @Override
    public void onResponse(LiveNationConfig response) {
        getAccountPresenters().getGetUser().initialize(getActivity(), null, AccountFragment.this);
        LiveNationApplication.getLocationProvider().reverseGeocodeCity(response.getLat(), response.getLng(), getActivity(), this);
    }

    @Override
    public void onErrorResponse(int errorCode) {}

    //Location update

    @Override
    public void onLocationUpdated(int mode, double lat, double lng) {
        LiveNationApplication.getLocationProvider().reverseGeocodeCity(lat, lng, getActivity(), this);
    }

    //Click listener

    private class OnOrdersClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Ticketing.showOrderHistory(getActivity());
        }
    }

    private class OnFavoriteClick implements View.OnClickListener {
        private final int showTab;

        public OnFavoriteClick(int showTab) {
            this.showTab = showTab;
        }

        @Override
        public void onClick(View v) {
            if (showTab == FavoritesFragment.ARG_VALUE_ARTISTS) {
                LiveNationAnalytics.track(AnalyticConstants.FAVORITES_ARTISTS_CELL_TAP);
            } else {
                LiveNationAnalytics.track(AnalyticConstants.FAVORITES_VENUES_CELL_TAP);
            }

            Intent intent = new Intent(getActivity(), FavoriteActivity.class);
            intent.putExtra(FavoritesFragment.ARG_SHOW_TAB, showTab);
            startActivity(intent);
        }
    }

    private class OnLocationClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            LiveNationAnalytics.track(AnalyticConstants.LOCATION_ICON_TAP);
            Intent intent = new Intent(getActivity(), LocationActivity.class);
            startActivity(intent);
        }
    }
}

/*
 * 
 * @author Charlie Chilton 2014/02/04
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
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
import com.livenation.mobile.android.platform.util.Logger;
import com.livenation.mobile.android.ticketing.Ticketing;

public class AccountFragment extends LiveNationFragment implements LocationManager.GetCityCallback, ApiServiceBinder {
    private Fragment profileFragment;
    private TextView locationText;

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

        OnFavoriteClick favoriteArtistOnClick = new OnFavoriteClick(FavoritesFragment.ARG_VALUE_ARTISTS);
        result.findViewById(R.id.account_detail_favorite_artists_container).setOnClickListener(favoriteArtistOnClick);

        OnFavoriteClick favoriteVenueOnClick = new OnFavoriteClick(FavoritesFragment.ARG_VALUE_VENUES);
        result.findViewById(R.id.account_detail_favorite_venues_container).setOnClickListener(favoriteVenueOnClick);

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
        Logger.log("Accounts", "API binded");
        refreshUser(LoginHelper.isLoggin());
        getLocationManager().reverseGeocodeCity(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng(), getActivity(), this);
    }

    @Override
    public void onApiServiceNotAvailable() {

    }

    public void refreshUser(boolean isLoggedIn) {
        if (null != profileFragment) {
            removeFragment(profileFragment);
            profileFragment = null;
        }

        if (!isLoggedIn) {
            profileFragment = new AccountSignInFragment();
        } else {
            profileFragment = new AccountUserFragment();
        }

        addFragment(R.id.account_header_provider_container, profileFragment, "account_provider");
    }

    @Override
    public void onGetCity(City city) {
        locationText.setText(city.getName());
    }

    @Override
    public void onGetCityFailure() {
        locationText.setText("Geocode failed!");
    }

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

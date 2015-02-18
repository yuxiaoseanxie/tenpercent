/*
 * 
 * @author Charlie Chilton 2014/02/04
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.ConfigFilePersistenceHelper;
import com.livenation.mobile.android.na.helpers.LocationUpdateReceiver;
import com.livenation.mobile.android.na.helpers.LoginHelper;
import com.livenation.mobile.android.na.ui.FavoriteActivity;
import com.livenation.mobile.android.na.ui.LocationActivity;
import com.livenation.mobile.android.na.ui.OrderHistoryActivity;
import com.livenation.mobile.android.na.ui.dialogs.CommerceUnavailableDialogFragment;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.City;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

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

public class AccountFragment extends LiveNationFragment implements BasicApiCallback<City>, LocationUpdateReceiver.LocationUpdateListener {
    private final String LOCATION_NAME = "location";
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
        //Do not set RetainInstance to true because it sets the savedInstanceState to null
        //setRetainInstance(false);
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

        if (savedInstanceState == null) {
            //Get location for update the screen
            LiveNationApplication.getLocationProvider().getLocation(this);
        } else {
            String locationName = savedInstanceState.getString(LOCATION_NAME);
            locationText.setText(locationName);
        }

        refreshUser(LoginHelper.isLogout());
        //Register for being notify when the location change
        registerBroadcastReceiverForUpdate();

        return result;
    }

    private void registerBroadcastReceiverForUpdate() {
        final Context context = getActivity();
        LocalBroadcastManager.getInstance(context).registerReceiver(locationUpdateReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOCATION_UPDATE_INTENT_FILTER));
        LocalBroadcastManager.getInstance(context).registerReceiver(loginLogoutReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGIN_INTENT_FILTER));
        LocalBroadcastManager.getInstance(context).registerReceiver(loginLogoutReceiver, new IntentFilter(com.livenation.mobile.android.platform.Constants.LOGOUT_INTENT_FILTER));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Context context = getActivity();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(locationUpdateReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(loginLogoutReceiver);
    }

    public void refreshUser(boolean isLogout) {
        String fragmentClassNameToRemove;
        String fragmentClassNameToAdd;
        Fragment fragmentToAdd;

        if (isLogout) {
            fragmentClassNameToRemove = AccountUserFragment.class.getSimpleName();
            fragmentClassNameToAdd = AccountSignInFragment.class.getSimpleName();
            fragmentToAdd = new AccountSignInFragment();
        } else {
            fragmentClassNameToRemove = AccountSignInFragment.class.getSimpleName();
            fragmentClassNameToAdd = AccountUserFragment.class.getSimpleName();
            fragmentToAdd = new AccountUserFragment();
        }

        Fragment fragmentToRemove = getChildFragmentManager().findFragmentByTag(fragmentClassNameToRemove);
        if (null != fragmentToRemove) {
            removeFragment(fragmentToRemove);
        }

        if (null == getChildFragmentManager().findFragmentByTag(fragmentClassNameToAdd)) {
            addFragment(R.id.account_header_provider_container, fragmentToAdd, fragmentClassNameToAdd);
        }
    }

    //Location update

    @Override
    public void onLocationUpdated(int mode, City city) {
        locationText.setText(city.getName());
    }

    @Override
    public void onResponse(City response) {
        if (!isAdded()) return;
        locationText.setText(response.getName());
    }

    @Override
    public void onErrorResponse(LiveNationError error) {
        //Never called
    }

    //Click listener

    private class OnOrdersClick implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            LiveNationAnalytics.track(AnalyticConstants.YOUR_ORDERS_TAP, AnalyticsCategory.DRAWER);

            if (getInstalledAppConfig().isCommerceAvailable()) {
                Intent intent = new Intent(view.getContext(), OrderHistoryActivity.class);
                view.getContext().startActivity(intent);
            } else {
                CommerceUnavailableDialogFragment dialogFragment = new CommerceUnavailableDialogFragment();
                dialogFragment.show(getFragmentManager(), CommerceUnavailableDialogFragment.TAG);
            }
        }

        private ConfigFilePersistenceHelper getInstalledAppConfig() {
            return LiveNationApplication.get().getInstalledAppConfig();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(LOCATION_NAME, locationText.getText().toString());
    }
}

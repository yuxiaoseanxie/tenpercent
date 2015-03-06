package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.uber.UberClient;
import com.livenation.mobile.android.na.uber.UberFragmentListener;
import com.livenation.mobile.android.na.uber.UberHelper;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.mobilitus.tm.tickets.models.Venue;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import rx.functions.Action1;

/**
 * Created by elodieferrais on 3/4/15.
 */
public class UberFragment extends Fragment implements UberFragmentListener {

    private final static String VENUE_LAT = "com.livenation.mobile.android.na.ui.fragments.UberFragment.VENUE_LAT";
    private final static String VENUE_LNG = "com.livenation.mobile.android.na.ui.fragments.UberFragment.VENUE_LNG";
    private final static String VENUE_ADDRESS = "com.livenation.mobile.android.na.ui.fragments.UberFragment.VENUE";
    private final static String VENUE_NAME = "com.livenation.mobile.android.na.ui.fragments.UberFragment.VENUE";
    private final static String UBER_LISTENER = "com.livenation.mobile.android.na.ui.fragments.UberFragment.UBER_LISTENER";
    private final static String UBER_MIN = "com.livenation.mobile.android.na.ui.fragments.UberFragment.UBER_MIN";
    private final static String UBER_ESTIMATES = "com.livenation.mobile.android.na.ui.fragments.UberFragment.UBER_ESTIMATES";


    private static final int ACTIVITY_RESULT_UBER = 1;
    private static UberClient uberClient;
    private float lat;
    private float lng;
    private String adress;
    private String name;
    private View rootView;

    private Integer minutes;
    private String estimates;

    public static UberFragment newInstance(Venue venue, Context context) {
        UberFragment fragment = new UberFragment();
        float lat = Double.valueOf(venue.getLatitude()).floatValue();
        float lng = Double.valueOf(venue.getLongitude()).floatValue();
        String venueAddress = UberHelper.getUberVenueAddress(venue);
        String venueName = UberHelper.getUberVenueName(venue);
        uberClient = new UberClient(context);

        /**Bundle bundle = new Bundle();
         bundle.putFloat(VENUE_LAT, lat);
         bundle.putFloat(VENUE_LNG, lng);
         bundle.putString(VENUE_ADDRESS, venueAddress);
         bundle.putString(VENUE_NAME, venueName);
         bundle.putSerializable(UBER_LISTENER, listener);**/

        Bundle bundle = new Bundle();
        Double[] array = {34.0878, -118.3722};

        bundle.putFloat(VENUE_LAT, 34.0878f);
        bundle.putFloat(VENUE_LNG, -118.3728f);
        bundle.putString(VENUE_ADDRESS, "8470 Santa Monica Blvd West Hollywood, CA 90069");
        bundle.putString(VENUE_NAME, "Irish Coffee Bistro");
        fragment.setArguments(bundle);
        return fragment;
    }

    public static UberFragment newInstance(com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue venue) {
        UberFragment fragment = new UberFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        lat = getArguments().getFloat(VENUE_LAT);
        lng = getArguments().getFloat(VENUE_LNG);
        adress = getArguments().getString(VENUE_ADDRESS);
        name = getArguments().getString(VENUE_NAME);
        minutes = getArguments().getInt(UBER_MIN);
        estimates = getArguments().getString(UBER_ESTIMATES);
    }


    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        lat = getArguments().getFloat(VENUE_LAT);
        lng = getArguments().getFloat(VENUE_LNG);
        adress = getArguments().getString(VENUE_ADDRESS);
        name = getArguments().getString(VENUE_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.order_uber_signup, container, false);
        if (minutes != null && name != null) {
            updateView();
        } else {
            fetchEstimation(this);
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_RESULT_UBER:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = UberHelper.getUberAppLaunchIntent(uberClient.getClientId(), data);
                    getActivity().startActivity(intent);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Intent intent = UberHelper.getUberAppLaunchIntent(uberClient.getClientId());
                    getActivity().startActivity(intent);
                }
                break;
        }
    }

    private void updateUberSignUpView(int min) {
        TextView title = (TextView) rootView.findViewById(R.id.uber_title);
        TextView subtitle = (TextView) rootView.findViewById(R.id.uber_subtitle);
        String uberTitle = getResources().getString(R.string.uber_order_book_ride_mins);
        uberTitle = String.format(uberTitle, min);
        title.setText(uberTitle);

        subtitle.setText(LiveNationApplication.get().getInstalledAppConfig().getUberFreeRideText());
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUberSignupClick(lat, lng, adress, name);
                trackUberAnalytics(false);
            }
        });
    }


    private void updateUberRideView(int min, String estimates) {
        ImageView icon = (ImageView) rootView.findViewById(R.id.uber_icon);
        TextView title = (TextView) rootView.findViewById(R.id.uber_title);
        TextView subtitle = (TextView) rootView.findViewById(R.id.uber_subtitle);
        String uberTitle = getResources().getString(R.string.uber_order_book_ride_mins);
        icon.setImageDrawable(getResources().getDrawable(R.drawable.uber_logo_icon));
        uberTitle = String.format(uberTitle, min);
        title.setText(uberTitle);
        subtitle.setText(estimates);

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUberRideClick(lat, lng, adress, name);
                trackUberAnalytics(true);
            }
        });
    }

    private void onUberSignupClick(float lat, float lng, String address, String name) {
        Intent intent = new Intent(Intent.ACTION_VIEW, UberHelper.getUberSignupLink(uberClient.getClientId(), lat, lng, address, name));
        startActivity(intent);
    }

    private void onUberRideClick(final float endLat, final float endLng, String address, String addressName) {
        DialogFragment dialog = UberHelper.getUberEstimateDialog(uberClient, endLat, endLng, address, addressName);
        dialog.setTargetFragment(this, ACTIVITY_RESULT_UBER);
        dialog.show(getFragmentManager(), UberDialogFragment.UBER_DIALOG_TAG);
    }

    private void trackUberAnalytics(boolean isUberInstalled) {
        Props props = new Props();
        String uber_app_value = AnalyticConstants.UBER_APP_UNINSTALLED;
        if (isUberInstalled) {
            uber_app_value = AnalyticConstants.UBER_APP_INSTALLED;
        }
        props.put(AnalyticConstants.UBER_APP, uber_app_value);
        LiveNationAnalytics.track(AnalyticConstants.UBER_YOUR_ORDERS_TAP, AnalyticsCategory.YOUR_ORDERS, props);
    }

    private void updateView() {
        if (UberHelper.isUberAppInstalled(getActivity())) {
            updateUberRideView(minutes, estimates);
        } else {
            updateUberSignUpView(minutes);
        }
    }

    public void fetchEstimation(final UberFragmentListener listener) {
        UberHelper.getQuickEstimate(uberClient, lat, lng)
                .subscribe(new Action1<LiveNationEstimate>() {
                    @Override
                    public void call(LiveNationEstimate liveNationEstimate) {
                        minutes = liveNationEstimate.getTime().getEstimateMins();
                        getArguments().putInt(UBER_MIN, minutes);
                        estimates = liveNationEstimate.getPrice().getEstimate();
                        getArguments().putString(UBER_ESTIMATES, estimates);
                        listener.onUberFragmentReady(UberFragment.this);
                        if (rootView != null) {
                            updateView();
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        listener.onUberFragmentNotAvailable(throwable);
                    }
                });
    }

    @Override
    public void onUberFragmentReady(UberFragment uberFragment) {
        updateView();
    }

    @Override
    public void onUberFragmentNotAvailable(Throwable error) {
        //Do nothing
    }

}


package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
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
import android.widget.Button;
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
    private final static String UBER_MIN = "com.livenation.mobile.android.na.ui.fragments.UberFragment.UBER_MIN";
    private final static String UBER_ESTIMATES = "com.livenation.mobile.android.na.ui.fragments.UberFragment.UBER_ESTIMATES";
    private final static String CATEGORY = "com.livenation.mobile.android.na.ui.fragments.UberFragment.CATEGORY";
    private final static String IS_BUTTON_VERSION = "com.livenation.mobile.android.na.ui.fragments.UberFragment.IS_BUTTON_VERSION";


    private static final int ACTIVITY_RESULT_UBER = 1;
    private static UberClient uberClient;
    private float lat;
    private float lng;
    private String adress;
    private String name;
    private AnalyticsCategory category;
    private boolean isButtonVersion = false;
    private View rootView;

    private Integer minutes;
    private String estimates;


    public static UberFragment newInstance(Venue venue, Context context, AnalyticsCategory category) {
        float lat = Double.valueOf(venue.getLatitude()).floatValue();
        float lng = Double.valueOf(venue.getLongitude()).floatValue();
        String venueAddress = UberHelper.getUberVenueAddress(venue);
        String venueName = UberHelper.getUberVenueName(venue);

        return init(lat, lng, venueAddress, venueName, context, category, false);
    }

    public static UberFragment newInstance(com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue venue, Context context, AnalyticsCategory category) {
        return newInstance(venue, context, category, false);
    }


    public static UberFragment newInstance(com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue venue, Context context, AnalyticsCategory category, boolean isButtonVersion) {
        Float endLat = null;
        Float endLng = null;
        if (venue.getLat() != null && venue.getLng() != null) {
            endLat = Double.valueOf(venue.getLat()).floatValue();
            endLng = Double.valueOf(venue.getLng()).floatValue();
        }
        String venueAddress = venue.getAddress().getSmallFriendlyAddress(false);
        String venueName = venue.getName();

        return init(endLat, endLng, venueAddress, venueName, context, category, isButtonVersion);
    }


    private static UberFragment init(float lat, float lng, String address, String name, Context context, AnalyticsCategory category, boolean isButtonVersion) {
        UberFragment fragment = new UberFragment();
        uberClient = new UberClient(context);

        Bundle bundle = new Bundle();
        bundle.putFloat(VENUE_LAT, lat);
        bundle.putFloat(VENUE_LNG, lng);
        bundle.putString(VENUE_ADDRESS, address);
        bundle.putString(VENUE_NAME, name);
        bundle.putSerializable(CATEGORY, category);
        bundle.putBoolean(IS_BUTTON_VERSION, isButtonVersion);

        /**Bundle bundle = new Bundle();
         Double[] array = {34.0878, -118.3722};

         bundle.putFloat(VENUE_LAT, lat);
         bundle.putFloat(VENUE_LNG, lng);
         bundle.putString(VENUE_ADDRESS, "8470 Santa Monica Blvd West Hollywood, CA 90069");
         bundle.putString(VENUE_NAME, "Irish Coffee Bistro");**/
        fragment.setArguments(bundle);
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
        category = (AnalyticsCategory) getArguments().getSerializable(CATEGORY);
        isButtonVersion = getArguments().getBoolean(IS_BUTTON_VERSION);
    }


    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        //Need those value assigned because they are used before onCreate()
        lat = getArguments().getFloat(VENUE_LAT);
        lng = getArguments().getFloat(VENUE_LNG);
        adress = getArguments().getString(VENUE_ADDRESS);
        name = getArguments().getString(VENUE_NAME);
        category = (AnalyticsCategory) getArguments().getSerializable(CATEGORY);
        isButtonVersion = getArguments().getBoolean(IS_BUTTON_VERSION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (isButtonVersion) {
            rootView = LayoutInflater.from(getActivity()).inflate(R.layout.view_uber_button, container, false);
        } else {
            rootView = LayoutInflater.from(getActivity()).inflate(R.layout.order_uber_signup, container, false);
        }
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
        if (!isButtonVersion) {
            TextView title = (TextView) rootView.findViewById(R.id.uber_title);
            TextView subtitle = (TextView) rootView.findViewById(R.id.uber_subtitle);
            String uberTitle = getResources().getString(R.string.uber_order_book_ride_mins);
            uberTitle = String.format(uberTitle, min);
            title.setText(uberTitle);

            subtitle.setText(LiveNationApplication.get().getInstalledAppConfig().getUberFreeRideText());
        }
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUberSignupClick(lat, lng, adress, name);
                UberHelper.trackUberTap(category);
            }
        });
    }


    private void updateUberRideView(int min, String estimates) {
        if (!isButtonVersion) {
            ImageView icon = (ImageView) rootView.findViewById(R.id.uber_icon);
            icon.setImageDrawable(getResources().getDrawable(R.drawable.uber_logo_icon));
            TextView title = (TextView) rootView.findViewById(R.id.uber_title);
            TextView subtitle = (TextView) rootView.findViewById(R.id.uber_subtitle);
            String uberTitle = getResources().getString(R.string.uber_order_book_ride_mins);
            uberTitle = String.format(uberTitle, min);
            title.setText(uberTitle);
            subtitle.setText(estimates);
        } else {
            ((Button) rootView).setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.uber_logo_icon), null, null);
            ((Button) rootView).setText(estimates);
        }

        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUberRideClick(lat, lng, adress, name);
                UberHelper.trackUberTap(category);
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


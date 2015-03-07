package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.listener.OnAddressClick;
import com.livenation.mobile.android.na.listener.OnPhoneNumberClick;
import com.livenation.mobile.android.na.listener.OnVenueDetailClick;
import com.livenation.mobile.android.na.listener.UberClick;
import com.livenation.mobile.android.na.uber.UberHelper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Address;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/26/15.
 */
public class VenueDetailFragment extends Fragment {
    private final static String VENUE = "com.livenation.mobile.android.na.ui.fragments.VenueDetailFragment.VENUE";
    private final static String WITH_UBER_RIDE_VENUE = "com.livenation.mobile.android.na.ui.fragments.VenueDetailFragment.WITH_UBER_RIDE_VENUE";

    private Venue venue;
    private Boolean withUberRide;

    private TextView location;
    private TextView telephone;
    private View venueInfo;
    private View phonebox;
    private Button uberButton;
    private View swoochdivider;

    public static VenueDetailFragment newInstance(Venue venue, boolean withUberRide) {
        VenueDetailFragment venueDetailFragment = new VenueDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(VENUE, venue);
        bundle.putSerializable(WITH_UBER_RIDE_VENUE, withUberRide);
        venueDetailFragment.setArguments(bundle);
        return venueDetailFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_venue_detail, container, false);

        venue = (Venue) getArguments().getSerializable(VENUE);
        withUberRide = getArguments().getBoolean(WITH_UBER_RIDE_VENUE, true);

        location = (TextView) result.findViewById(R.id.fragment_venue_detail_location);
        telephone = (TextView) result.findViewById(R.id.fragment_venue_detail_telephone);
        venueInfo = result.findViewById(R.id.fragment_venue_detail_venue_info_link);
        phonebox = result.findViewById(R.id.fragment_venue_detail_phone_box);
        uberButton = (Button) result.findViewById(R.id.fragment_venue_uber_button);
        swoochdivider = result.findViewById(R.id.fragment_venue_detail_swoochdivider);

        refresh();
        return result;
    }

    private void refresh() {

        //Address
        if (null != venue.getAddress()) {
            Address address = venue.getAddress();
            location.setText(address.getSmallFriendlyAddress(true));
        } else {
            location.setText("");
        }
        location.setOnClickListener(new OnAddressClick(venue, LiveNationApplication.get().getApplicationContext()));


        //Phone
        String phoneNumber = venue.getFormattedPhoneNumber();
        telephone.setText(phoneNumber);
        if (phoneNumber.isEmpty()) {
            phonebox.setVisibility(View.GONE);
        } else {
            telephone.setOnClickListener(new OnPhoneNumberClick(getActivity(), venue));
        }

        //+ Venue info
        if (venue.getBoxOffice() == null) {
            loadBoxOfficeInfo(venue.getNumericId());
        } else {
            displayBoxOfficeInfo(venue);
        }

        //Uber
        if (withUberRide && venue.getLat() != null && venue.getLng() != null) {
            uberButton.setOnClickListener(new UberClick(this, venue, AnalyticsCategory.ADP));
            swoochdivider.setVisibility(View.VISIBLE);
        } else {
            //hide travel options to unroutable venue
            uberButton.setVisibility(View.GONE);
            swoochdivider.setVisibility(View.GONE);
        }

    }

    private void loadBoxOfficeInfo(final long venueId) {
        LiveNationApplication.getLiveNationProxy().getSingleVenue(venueId, new BasicApiCallback<Venue>() {
            @Override
            public void onResponse(Venue fullVenue) {
                displayBoxOfficeInfo(fullVenue);
            }

            @Override
            public void onErrorResponse(LiveNationError error) {
                Log.e(getClass().getName(), "Could not load box office info. " + error);
            }
        });
    }

    private void displayBoxOfficeInfo(Venue venue) {
        if (venue.getBoxOffice() == null || venue.getBoxOffice().isEmpty()) {
            venueInfo.setVisibility(View.GONE);
            venueInfo.setOnClickListener(null);
        } else {
            venueInfo.setVisibility(View.VISIBLE);

            OnVenueDetailClick onVenueClick = new OnVenueDetailClick(getActivity(), venue);
            venueInfo.setOnClickListener(onVenueClick);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UberClick.ACTIVITY_RESULT_UBER:
                if (resultCode == Activity.RESULT_OK) {
                    Intent intent = UberHelper.getUberAppLaunchIntent(getResources().getString(R.string.uber_client_id), data);
                    startActivity(intent);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Intent intent = UberHelper.getUberAppLaunchIntent(getResources().getString(R.string.uber_client_id));
                    startActivity(intent);
                }
        }
    }

}

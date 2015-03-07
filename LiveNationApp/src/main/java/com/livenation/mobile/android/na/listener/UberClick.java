package com.livenation.mobile.android.na.listener;

import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.analytics.Props;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.uber.UberClient;
import com.livenation.mobile.android.na.uber.UberHelper;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.View;
import rx.functions.Action1;
import rx.subjects.ReplaySubject;

/**
 * Created by elodieferrais on 2/26/15.
 */
public class UberClick implements View.OnClickListener {
    public static final int ACTIVITY_RESULT_UBER = 1;
    private Venue venue;
    private Fragment fragment;
    private Context context;
    private UberClient uberClient;
    private AnalyticsCategory category;
    //create a replay subject for our fastest uber estimation.
    //this allows the API operation for the fastest uber to be cached, so the result is available instantly to anyone who subscribes to this object
    //This is useful when the UI that shows the estimate is repeatedly created and destroyed, as we can cache the estimate
    private ReplaySubject<LiveNationEstimate> fastestUber = ReplaySubject.create(1);


    public UberClick(Fragment fragment, Venue venue, AnalyticsCategory category) {
        this.venue = venue;
        this.fragment = fragment;
        this.category = category;
        this.context = fragment.getActivity().getApplicationContext();
        uberClient = new UberClient(context);

        UberHelper.getQuickEstimate(uberClient, Float.valueOf(venue.getLat()), Float.valueOf(venue.getLng())).
                subscribe(new Action1<LiveNationEstimate>() {
                    @Override
                    public void call(LiveNationEstimate liveNationEstimate) {
                        fastestUber.onNext(liveNationEstimate);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        //Error Handler.
                        //API call failed, distance, network error, and so on.
                        //Leave the default UI with no estimate text
                    }
                });
    }

    @Override
    public void onClick(View v) {
        //Analytics
        UberHelper.trackUberTap(category);
        if (AnalyticsHelper.isAppInstalled(ExternalApplicationAnalytics.UBER.getPackageName(), context)) {
            //show uber price estimates
            showEstimates(venue);
        } else {
            //no uber app installed, show sign up link
            goUberSignup(venue);
        }

    }

    private void showEstimates(Venue venue) {
        float endLat = Double.valueOf(venue.getLat()).floatValue();
        float endLng = Double.valueOf(venue.getLng()).floatValue();
        String venueAddress = venue.getAddress().getSmallFriendlyAddress(false);
        String venueName = venue.getName();

        DialogFragment dialog = UberHelper.getUberEstimateDialog(uberClient, endLat, endLng, venueAddress, venueName);

        dialog.setTargetFragment(fragment, ACTIVITY_RESULT_UBER);
        dialog.show(fragment.getFragmentManager(), UberDialogFragment.UBER_DIALOG_TAG);
    }

    private void goUberSignup(Venue venue) {
        UberHelper.trackUberkWebLaunch(category);

        float endLat = Double.valueOf(venue.getLat()).floatValue();
        float endLng = Double.valueOf(venue.getLng()).floatValue();
        String venueAddress = venue.getAddress().getSmallFriendlyAddress(false);
        String venueName = venue.getName();

        Intent intent = new Intent(Intent.ACTION_VIEW, UberHelper.getUberSignupLink(uberClient.getClientId(), endLat, endLng, venueName, venueAddress));
        fragment.startActivity(intent);
    }
}

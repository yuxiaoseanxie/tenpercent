package com.livenation.mobile.android.na.uber;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.livenation.mobile.android.na.ObservableTemporaryUtils.ObservableProvider;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.mobilitus.tm.tickets.models.Venue;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by cchilton on 12/2/14.
 */
public class UberHelper {

    public static DialogFragment getUberEstimateDialog(final UberClient uberClient, final float endLat, final float endLng, String address, String addressName) {
        final UberDialogFragment dialog = UberDialogFragment.newInstance(endLat, endLng, address, addressName);

        final Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                dialog.onUberError();
            }
        };

        //get current location
        ObservableProvider.getObservableLocation().subscribe(new Action1<Double[]>() {
            @Override
            public void call(Double[] startPoint) {
                //get estimates
                uberClient.getEstimates(startPoint[0].floatValue(), startPoint[1].floatValue(), endLat, endLng)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<LiveNationEstimate>>() {
                            @Override
                            public void call(ArrayList<LiveNationEstimate> liveNationEstimates) {
                                //Display estimates
                                dialog.setPriceEstimates(liveNationEstimates);
                            }
                        }, onError, new Action0() {
                            @Override
                            public void call() {
                                Log.d("aa", "aa");
                            }
                        });
            }
        }, onError);
        return dialog;
    }

    public static Intent getUberAppLaunchIntent(UberClient uberClient, Intent data) {
        LiveNationEstimate estimate = (LiveNationEstimate) data.getSerializableExtra(UberDialogFragment.EXTRA_RESULT_ESTIMATE);
        String productId = estimate.getProduct().getProductId();
        String address = data.getStringExtra(UberDialogFragment.EXTRA_RESULT_ADDRESS);
        String addressName = data.getStringExtra(UberDialogFragment.EXTRA_RESULT_NAME);
        Float lat = data.getFloatExtra(UberDialogFragment.EXTRA_RESULT_LATITUDE, 0f);
        Float lng = data.getFloatExtra(UberDialogFragment.EXTRA_RESULT_LONGITUDE, 0f);

        Uri uri = uberClient.getUberLaunchUri(productId, lat, lng, addressName, address);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        return intent;
    }

    public static String getUberVenueAddress(Venue venue) {
        if (venue == null) return "";
        List<String> addresses = new ArrayList<String>();
        addresses.add(venue.getAddress1());
        addresses.add(venue.getAddress2());
        addresses.add(venue.getState());

        StringBuilder out = new StringBuilder();
        for (String address : addresses) {
            if (address != null && address.length() > 0) {
                if (out.length() > 0) {
                    out.append(", ");
                }
                out.append(address);
            }
        }
        return out.toString();
    }


    public static String getUberVenueName(Venue venue) {
        if (venue == null) return "";
        return venue.getName();
    }
}

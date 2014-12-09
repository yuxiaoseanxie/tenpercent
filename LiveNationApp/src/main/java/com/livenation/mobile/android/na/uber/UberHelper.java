package com.livenation.mobile.android.na.uber;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import com.livenation.mobile.android.na.ObservableTemporaryUtils.ObservableProvider;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.mobilitus.tm.tickets.models.Venue;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

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
            public void call(final Double[] startPoint) {
                //get estimates
                uberClient.getEstimates(startPoint[0].floatValue(), startPoint[1].floatValue(), endLat, endLng)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<LiveNationEstimate>>() {
                            @Override
                            public void call(ArrayList<LiveNationEstimate> liveNationEstimates) {
                                //Display estimates
                                dialog.setPriceEstimates(liveNationEstimates);
                                dialog.setOriginLocation(startPoint[0].floatValue(), startPoint[1].floatValue());
                            }
                        }, onError, new Action0() {
                            @Override
                            public void call() {
                            }
                        });
            }
        }, onError);
        return dialog;
    }

    public static Intent getUberAppLaunchIntent(String clientId) {
        return getUberAppLaunchIntent(clientId, null);
    }

    public static Intent getUberAppLaunchIntent(String clientId, Intent data) {
        if (data == null || !data.hasExtra(UberDialogFragment.EXTRA_RESULT_ESTIMATE)) {
            Uri uri = getUberAppLaunchUri(clientId);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            return intent;
        }

        LiveNationEstimate estimate = (LiveNationEstimate) data.getSerializableExtra(UberDialogFragment.EXTRA_RESULT_ESTIMATE);
        String productId = estimate.getProduct().getProductId();
        String address = data.getStringExtra(UberDialogFragment.EXTRA_RESULT_ADDRESS);
        String addressName = data.getStringExtra(UberDialogFragment.EXTRA_RESULT_NAME);
        Float lat = data.getFloatExtra(UberDialogFragment.EXTRA_RESULT_LATITUDE, 0f);
        Float lng = data.getFloatExtra(UberDialogFragment.EXTRA_RESULT_LONGITUDE, 0f);

        Uri uri = getUberAppLaunchUri(clientId, productId, lat, lng, addressName, address);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        return intent;
    }

    public static Uri getUberSignupLink(String clientId) {
        return Uri.parse(String.format("https://m.uber.com/sign-up?client_id=%s", clientId));
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

    public static boolean isUberAppInstalled(Context context) {
        return AnalyticsHelper.isAppInstalled(ExternalApplicationAnalytics.UBER.getPackageName(), context);
    }

    public static Observable<LiveNationEstimate> getQuickEstimate(final UberClient uberClient, final float startLat, final float startLng, final float endLat, final float endLng) {
        final PublishSubject<LiveNationEstimate> result = PublishSubject.create();

        uberClient.getEstimates(startLat, startLng, endLat, endLng).
                subscribeOn(Schedulers.io()).
                observeOn(AndroidSchedulers.mainThread()).
                filter(new Func1<ArrayList<LiveNationEstimate>, Boolean>() {
                    @Override
                    public Boolean call(ArrayList<LiveNationEstimate> liveNationEstimates) {
                        //filter out any api responses with no uber suggestions (eg for people in france :) )
                        return (liveNationEstimates.size() > 0);
                    }
                }).
                subscribe(new Action1<ArrayList<LiveNationEstimate>>() {
                    @Override
                    public void call(ArrayList<LiveNationEstimate> liveNationEstimates) {
                        //select the fastest uber product from the results
                        LiveNationEstimate cheapest = null;
                        for (LiveNationEstimate estimate : liveNationEstimates) {
                            if (cheapest == null) {
                                if (estimate.hasPrice() && estimate.getPrice().getLowEstimate() != null) {
                                    cheapest = estimate;
                                }
                                continue;
                            }

                            if (!estimate.hasPrice() || estimate.getPrice().getLowEstimate() == null)
                                continue;

                            //check whether the current iteration is cheaper than our current target
                            if (estimate.getPrice().getLowEstimate() < cheapest.getPrice().getLowEstimate()) {
                                cheapest = estimate;
                            }
                        }
                        //emit the fastest result
                        result.onNext(cheapest);
                    }
                });

        return result;
    }

    public static Observable<LiveNationEstimate> getQuickEstimate(final UberClient uberClient, final float endLat, final float endLng) {
        final PublishSubject<LiveNationEstimate> result = PublishSubject.create();

        ObservableProvider.getObservableLocation().subscribe(new Action1<Double[]>() {
            @Override
            public void call(Double[] doubles) {
                float lat = doubles[0].floatValue();
                float lng = doubles[1].floatValue();
                //perform api request
                getQuickEstimate(uberClient, lat, lng, endLat, endLng).
                        subscribe(new Action1<LiveNationEstimate>() {
                            @Override
                            public void call(LiveNationEstimate liveNationEstimate) {
                                result.onNext(liveNationEstimate);
                            }
                        });
            }
        });

        return result;
    }

    private static Uri getUberAppLaunchUri(String clientId, String productId, float dropoffLat, float dropoffLng, String dropoffName, String dropoffAddress) {
        Uri uberUri = getUberAppLaunchUri(clientId);
        Uri.Builder builder = uberUri.buildUpon();

        builder.appendQueryParameter("dropoff[latitude]", Float.valueOf(dropoffLat).toString());
        builder.appendQueryParameter("dropoff[longitude]", Float.valueOf(dropoffLng).toString());
        builder.appendQueryParameter("dropoff[formatted_address]", dropoffAddress);
        builder.appendQueryParameter("product_id", productId);
        builder.appendQueryParameter("dropoff[nickname]", dropoffName);

        return builder.build();
    }

    private static Uri getUberAppLaunchUri(String clientId) {
        Uri uberUri = Uri.parse("uber://");
        Uri.Builder builder = uberUri.buildUpon();

        builder.appendQueryParameter("action", "setPickup");
        builder.appendQueryParameter("client_id", clientId);
        builder.appendQueryParameter("pickup", "my_location");

        return builder.build();
    }
}

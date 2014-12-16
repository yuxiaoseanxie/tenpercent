package com.livenation.mobile.android.na.uber;

import android.content.Context;

import com.livenation.mobile.android.na.ObservableTemporaryUtils.ObservableProvider;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.ArrayList;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by elodieferrais on 12/15/14.
 */
public class UberProgression {
    private final UberClient uberClient;
    private Boolean isUberAvailableCached;
    private ArrayList<LiveNationEstimate> liveNationEstimates;

    public UberProgression(Context context) {
        this.uberClient = new UberClient(context);
    }

    public void isUberAvailable(final UberProgressionAdpater adapter, final float destLat, final float destLng) {
        if (isUberAvailableCached != null) {
            if (isUberAvailableCached) {
                adapter.onUberAvailable(liveNationEstimates);
            } else {
                adapter.onUberNotAvailable(null);
            }
        }

        final Action1<Throwable> onError = new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                adapter.onUberNotAvailable(new LiveNationError(throwable));
                isUberAvailableCached = false;
            }
        };

        //get current location
        ObservableProvider.getObservableLocation().subscribe(new Action1<Double[]>() {
            @Override
            public void call(Double[] startPoint) {
                //get estimates
                uberClient.getEstimates(startPoint[0].floatValue(), startPoint[1].floatValue(), destLat, destLng)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<ArrayList<LiveNationEstimate>>() {
                            @Override
                            public void call(ArrayList<LiveNationEstimate> liveNationEstimates) {
                                UberProgression.this.liveNationEstimates = liveNationEstimates;
                                if (liveNationEstimates.isEmpty()) {
                                    adapter.onUberNotAvailable(null);
                                    return;
                                }

                                adapter.onUberAvailable(liveNationEstimates);
                                isUberAvailableCached = true;
                            }
                        }, onError);
            }
        }, onError);
    }

    public Boolean getIsUberAvailableCached() {
        return isUberAvailableCached;
    }

    public ArrayList<LiveNationEstimate> getEstimates() {
        return liveNationEstimates;
    }

    public interface UberProgressionAdpater {
        void onUberAvailable(ArrayList<LiveNationEstimate> liveNationEstimates);

        void onUberNotAvailable(LiveNationError error);
    }

}

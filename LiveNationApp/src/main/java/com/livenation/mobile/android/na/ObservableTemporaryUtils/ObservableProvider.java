package com.livenation.mobile.android.na.ObservableTemporaryUtils;

import com.livenation.mobile.android.na.providers.location.DeviceLocationProvider;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by elodieferrais on 11/25/14.
 */
public class ObservableProvider {
    public static Observable<Double[]> getObservableLocation() {
        Observable<Double[]> observable = Observable.create(new Observable.OnSubscribe<Double[]>() {
            @Override
            public void call(final Subscriber<? super Double[]> subscriber) {
                LocationProvider locationProvider = new DeviceLocationProvider();
                locationProvider.getLocation(new ProviderCallback<Double[]>() {
                    @Override
                    public void onResponse(Double[] response) {
                        subscriber.onNext(response);
                    }

                    @Override
                    public void onErrorResponse() {
                        subscriber.onError(null);
                    }
                });
            }
        });
        return observable;
    }
}

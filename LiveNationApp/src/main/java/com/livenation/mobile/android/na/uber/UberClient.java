package com.livenation.mobile.android.na.uber;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.ExternalApplicationAnalytics;
import com.livenation.mobile.android.na.providers.location.DeviceLocationProvider;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.uber.service.model.UberPrice;
import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProduct;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;
import com.livenation.mobile.android.na.uber.service.model.UberTime;
import com.livenation.mobile.android.na.uber.service.model.UberTimeResponse;
import com.livenation.mobile.android.platform.init.callback.ProviderCallback;
import com.livenation.mobile.android.platform.init.provider.LocationProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;

/**
 * Created by cchilton on 11/20/14.
 */
public class UberClient {
    //TEMPORARY TESTING TOKEN!
    private final static String API_SERVER_TOKEN = "n_8uFl4o06CW6hZinwNV68aitRno92eWpfgFIjcp";
    private final static String API_PARAM_TOKEN_NAME = "server_token";
    private final static String API_ENDPOINT = "https://api.uber.com";
    private final Context context;
    private final String clientId;
    private final UberService service;

    public UberClient(Context context) {
        this.context = context.getApplicationContext();
        this.clientId = context.getString(R.string.uber_client_id);
        this.service = createUberService();
    }

    public UberService getService() {
        return service;
    }

    public boolean isUberAppInstalled() {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(ExternalApplicationAnalytics.UBER.getPackageName(), PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            //no uber app installed
        }
        return false;
    }

    public Uri getUberSignupLink() {
        return Uri.parse(String.format("https://m.uber.com/sign-up?client_id=%s", clientId));
    }

    public Uri getUberLaunchUri(String productId, float pickupLat, float pickupLng, float dropoffLat, float dropoffLng, String dropoffName, String dropoffAddress) {
        Uri uberUri = Uri.parse("uber://");
        Uri.Builder builder = uberUri.buildUpon();

        builder.appendQueryParameter("action", "setPickup");
        builder.appendQueryParameter("client_id", clientId);
        builder.appendQueryParameter("pickup", "my_location");
        builder.appendQueryParameter("pickup[latitude]", Float.valueOf(pickupLat).toString());
        builder.appendQueryParameter("pickup[longitude]", Float.valueOf(pickupLng).toString());
        builder.appendQueryParameter("dropoff[latitude]", Float.valueOf(dropoffLat).toString());
        builder.appendQueryParameter("dropoff[longitude]", Float.valueOf(dropoffLng).toString());
        builder.appendQueryParameter("product_id", productId);
        builder.appendQueryParameter("dropoff[nickname]", dropoffName);

        return builder.build();
    }

    public DialogFragment getUberDialogFragment(final float endLat, final float endLng) {
        final UberDialogFragment dialog = UberDialogFragment.newInstance(null);

        Observable<Double[]> locationProvider = getObservableLocation();

        final Action1 onError = new Action1() {
            @Override
            public void call(Object o) {
                dialog.onUberError();
            }
        };

        locationProvider.subscribe(new Action1<Double[]>() {
            @Override
            public void call(Double[] doubles) {
                float startLat = doubles[0].floatValue();
                float startLng = doubles[1].floatValue();
                //prep observable Uber API call 1
                Observable<UberProductResponse> productsObservable = service.getProducts(startLat, startLng);

                //prep observable Uber API call 2
                Observable<UberPriceResponse> pricesObservable = service.getEstimates(startLat, startLng, endLat, endLng);

                //prep observable Uber API call 3
                Observable<UberTimeResponse> timesObservable = service.getTimes(startLat, startLng);

                //Declare our little error handler (could do this inline below, but this is more verbose

                //listen for both API calls to complete
                Observable.combineLatest(productsObservable, pricesObservable, timesObservable, new Func3<UberProductResponse, UberPriceResponse, UberTimeResponse, ArrayList<LiveNationEstimate>>() {
                    @Override
                    public ArrayList<LiveNationEstimate> call(UberProductResponse uberProducts, UberPriceResponse uberPrices, UberTimeResponse uberTimes) {
                        //once both API calls complete, merge the result of both calls into one list
                        //Note: neither the API calls nor this operation occur on the UI thread
                        return getProductEstimates(uberPrices.getPrices(), uberProducts.getProducts(), uberTimes.getTimes());
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<ArrayList<LiveNationEstimate>>() {
                    @Override
                    public void call(ArrayList<LiveNationEstimate> priceCapacities) {
                        //Execute provided callback with dialog object on Androids UI thread

                        dialog.setPriceEstimates(priceCapacities);
                    }

                }, onError);
            }
        }, onError);

        return dialog;
    }

    public Observable<UberTimeResponse> getQuickEstimate(float lat, float lng) {
        Observable<UberTimeResponse> timeObservable = service.getTimes(lat, lng);
        return timeObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Merge function that combines the result of the Uber Products() API response and the Uber Prices() API endpoint
     * <p/>
     * Uber's Fare Estimate (Prices) objects do not contain the capacity information for the product.
     * We retrieve the capacity information via a separate API call. This function merges the data
     * objects from the two API calls into one list.
     *
     * @param prices   A list of Uber fare estimations between point A and B
     * @param products A list of Uber products offered at point A
     * @return A merged list of "LiveNationEstimate" which contains a fare estimate and its associated Uber Product. The
     * Product will be null if no matching Fare->Product is found
     */
    private ArrayList<LiveNationEstimate> getProductEstimates(List<UberPrice> prices, List<UberProduct> products, List<UberTime> times) {
        ArrayList<LiveNationEstimate> estimates = new ArrayList<LiveNationEstimate>();
        Map<String, UberProduct> productMap = new HashMap<String, UberProduct>();
        Map<String, UberTime> timeMap = new HashMap<String, UberTime>();

        for (UberProduct product : products) {
            productMap.put(product.getProductId(), product);
        }

        for (UberTime time : times) {
            timeMap.put(time.getProductId(), time);
        }

        for (UberPrice price : prices) {
            UberProduct product = productMap.get(price.getProductId());
            UberTime time = timeMap.get(price.getProductId());
            LiveNationEstimate estimate = new LiveNationEstimate(price, product, time);
            estimates.add(estimate);
        }

        return estimates;
    }

    private UberService createUberService() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setEndpoint(API_ENDPOINT);
        builder.setConverter(new JacksonConverter(objectMapper));
        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam(API_PARAM_TOKEN_NAME, API_SERVER_TOKEN);
            }
        });

        if (BuildConfig.DEBUG) {
            builder.setLogLevel(RestAdapter.LogLevel.FULL);
        }
        return builder.build().create(UberService.class);
    }

    private Observable<Double[]> getObservableLocation() {
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

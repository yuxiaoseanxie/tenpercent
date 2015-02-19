package com.livenation.mobile.android.na.uber;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.helpers.VisibleForTesting;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.uber.service.model.UberPrice;
import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProduct;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;
import com.livenation.mobile.android.na.uber.service.model.UberTime;
import com.livenation.mobile.android.na.uber.service.model.UberTimeResponse;
import com.livenation.mobile.android.platform.api.transport.error.UnexpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.support.annotation.NonNull;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.converter.JacksonConverter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func3;

/**
 * Created by cchilton on 11/20/14.
 */
public class UberClient {
    //TEMPORARY TESTING TOKEN!
    private final static String API_PARAM_TOKEN_NAME = "server_token";
    private final static String API_ENDPOINT = "https://api.uber.com";
    private final Context context;
    private final String clientId;
    private final UberService service;
    private final String token;

    public UberClient(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.clientId = context.getString(R.string.uber_client_id);
        this.service = createUberService(null);
        this.token = context.getString(R.string.uber_token);
    }

    @VisibleForTesting
    public UberClient(@NonNull Context context, @NonNull Client retroClient, @NonNull String clientId, @NonNull String token) {
        this.context = context.getApplicationContext();
        this.clientId = clientId;
        this.service = createUberService(retroClient);
        this.token = token;
    }

    @VisibleForTesting
    public UberService getService() {
        return service;
    }

    public String getClientId() {
        return clientId;
    }

    public Observable<ArrayList<LiveNationEstimate>> getEstimates(final float startLat, final float startLng, final float endLat, final float endLng) {
        Observable<ArrayList<LiveNationEstimate>> observable = Observable.create(new Observable.OnSubscribe<ArrayList<LiveNationEstimate>>() {
            @Override
            public void call(final Subscriber<? super ArrayList<LiveNationEstimate>> subscriber) {
                Observable<UberProductResponse> productsObservable = service.getProducts(startLat, startLng);
                //prep observable Uber API call 2
                Observable<UberPriceResponse> pricesObservable = service.getEstimates(startLat, startLng, endLat, endLng);
                //prep observable Uber API call 3
                Observable<UberTimeResponse> timesObservable = service.getTimes(startLat, startLng);
                //Declare our little error handler (could do this inline below, but this is more verbose
                //listen for both API calls to complete
                Observable.combineLatest(productsObservable, pricesObservable, timesObservable, new Func3<UberProductResponse,
                        UberPriceResponse, UberTimeResponse, ArrayList<LiveNationEstimate>>() {
                    @Override
                    public ArrayList<LiveNationEstimate> call(UberProductResponse uberProducts, UberPriceResponse uberPrices, UberTimeResponse uberTimes) {
                        //once both API calls complete, merge the result of both calls into one list
                        //Note: neither the API calls nor this operation occur on the UI thread
                        return getProductEstimates(uberPrices.getPrices(), uberProducts.getProducts(), uberTimes.getTimes());
                    }
                }).subscribe(new Subscriber<ArrayList<LiveNationEstimate>>() {
                    @Override
                    public void onCompleted() {
                        subscriber.onStart();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override
                    public void onNext(ArrayList<LiveNationEstimate> liveNationEstimates) {
                        if (liveNationEstimates == null || liveNationEstimates.isEmpty()) {
                            onError(new Exception("Uber returned an empty List"));
                        } else {
                            subscriber.onNext(liveNationEstimates);
                        }
                    }
                });
            }

        });

        return observable;
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

    private UberService createUberService(Client retroClient) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setEndpoint(API_ENDPOINT);
        if (retroClient != null) {
            builder.setClient(retroClient);
        }
        builder.setConverter(new JacksonConverter(objectMapper));
        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam(API_PARAM_TOKEN_NAME, token);
            }
        });

        if (BuildConfig.DEBUG) {
            builder.setLogLevel(RestAdapter.LogLevel.FULL);
        }
        return builder.build().create(UberService.class);
    }
}

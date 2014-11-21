package com.livenation.mobile.android.na.uber;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.na.BuildConfig;
import com.livenation.mobile.android.na.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.uber.service.model.UberPrice;
import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProduct;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;
import com.livenation.mobile.android.na.uber.service.model.UberTime;
import com.livenation.mobile.android.na.uber.service.model.UberTimeResponse;

/**
 * Created by cchilton on 11/20/14.
 */
public class UberClient {
    //TEMPORARY TESTING TOKEN!
    private final static String API_SERVER_TOKEN = "n_8uFl4o06CW6hZinwNV68aitRno92eWpfgFIjcp";
    private final static String API_PARAM_TOKEN_NAME = "server_token";
    private final static String API_ENDPOINT = "https://api.com.livenation.mobile.android.na.uber.com";
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
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            //no com.livenation.mobile.android.na.uber app installed
        }
        return false;
    }

    public String getUberSignupLink() {
        return String.format("https://m.com.livenation.mobile.android.na.uber.com./sign-up?client_id=%s", clientId);
    }

    public Uri getUberLaunchUri(String productId, float pickupLat, float pickupLng, float dropoffLat, float dropoffLng, String dropoffName, String dropoffAddress) {
        Uri uberUri = Uri.parse("com.livenation.mobile.android.na.uber://");
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

    public void getUberDialogFragment(float startLat, float startLng, float endLat, float endLng, final UberDialogCallback callback) {
        //prep observable Uber API call 1
        Observable<UberProductResponse> productsObservable = service.getProducts(startLat, startLng);

        //prep observable Uber API call 2
        Observable<UberPriceResponse> pricesObservable = service.getEstimates(startLat, startLng, endLat, endLng);

        //prep observable Uber API call 3
        Observable<UberTimeResponse> timesObservable = service.getTimes(startLat, startLng);

        //Declare our little error handler (could do this inline below, but this is more verbose
        Action1 onError = new Action1() {
            @Override
            public void call(Object o) {
                callback.onGetUberDialogError();
            }
        };

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
                DialogFragment dialog = UberDialogFragment.newInstance(priceCapacities);
                callback.onGetUberDialogComplete(dialog);
            }

        }, onError);
    }

    public View getUberMenuItemView(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.menu_view_uber_estimation, parent, false);
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

    public static interface UberDialogCallback {
        void onGetUberDialogComplete(DialogFragment dialog);

        void onGetUberDialogError();
    }
}

package com.livenation.mobile.android.na.uber;


import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.na.uber.dialogs.UberDialogFragment;
import com.livenation.mobile.android.na.uber.service.UberService;
import com.livenation.mobile.android.na.uber.service.model.LiveNationEstimate;
import com.livenation.mobile.android.na.uber.service.model.UberPrice;
import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProduct;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;

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
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * Created by cchilton on 11/17/14.
 */
public class UberHelper {
    //TEMPORARY TESTING TOKEN!
    private final static String API_SERVER_TOKEN = "n_8uFl4o06CW6hZinwNV68aitRno92eWpfgFIjcp";
    private final static String API_PARAM_TOKEN_NAME = "server_token";
    private final static String API_ENDPOINT = "https://api.uber.com";

    public static UberService getUberService() {
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

    /**
     * Merge function that combines the result of the Uber Products() API response and the Uber Prices() API endpoint
     *
     * Uber's Fare Estimate (Prices) objects do not contain the capacity information for the product.
     * We retrieve the capacity information via a separate API call. This function merges the data
     * objects from the two API calls into one list.
     *
     * @param prices A list of Uber fare estimations between point A and B
     * @param products A list of Uber products offered at point A
     * @return A merged list of "LiveNationEstimate" which contains a fare estimate and its associated Uber Product. The
     * Product will be null if no matching Fare->Product is found
     */
    public static ArrayList<LiveNationEstimate> getProductEstimates(List<UberPrice> prices, List<UberProduct> products) {
        ArrayList<LiveNationEstimate> estimates = new ArrayList<LiveNationEstimate>();
        Map<String, UberProduct> productMap = new HashMap<String, UberProduct>();

        for (UberProduct product : products) {
            productMap.put(product.getProductId(), product);
        }

        for (UberPrice price : prices) {
            UberProduct product = productMap.get(price.getProductId());
            LiveNationEstimate estimate = new LiveNationEstimate(price, product);
            estimates.add(estimate);
        }

        return estimates;
    }

    public static boolean isUberAppInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo("com.ubercab", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            //no uber app installed
        }
        return false;
    }

    public static void getUberDialogFragment(float startLat, float startLng, float endLat, float endLng, final UberDialogCallback callback) {
        UberService service = getUberService();
        //prep observable Uber API call 1
        Observable<UberProductResponse> productsObservable = service.getProducts(startLat, startLng);

        //prep observable Uber API call 2
        Observable<UberPriceResponse> pricesObservable = service.getEstimates(startLat, startLng, endLat, endLng);

        //Declare our little error handler (could do this inline below, but this is more verbose
        Action1 onError = new Action1() {
            @Override
            public void call(Object o) {
                callback.onGetUberDialogError();
            }
        };

        //listen for both API calls to complete
        Observable.combineLatest(productsObservable, pricesObservable, new Func2<UberProductResponse, UberPriceResponse, ArrayList<LiveNationEstimate>>() {
            @Override
            public ArrayList<LiveNationEstimate> call(UberProductResponse uberProducts, UberPriceResponse uberPrices) {
                //once both API calls complete, merge the result of both calls into one list
                //Note: neither the API calls nor this operation occur on the UI thread
                return UberHelper.getProductEstimates(uberPrices.getPrices(),uberProducts.getProducts());
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

    public static interface UberDialogCallback {
        void onGetUberDialogComplete(DialogFragment dialog);
        void onGetUberDialogError();
    }

}

package com.livenation.mobile.android.na.uber.service;


import com.livenation.mobile.android.na.uber.service.model.UberPriceResponse;
import com.livenation.mobile.android.na.uber.service.model.UberProductResponse;
import com.livenation.mobile.android.na.uber.service.model.UberTimeResponse;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by cchilton on 11/17/14.
 */
public interface UberService {

    @GET("/v1/products")
    Observable<UberProductResponse> getProducts(@Query("latitude") float latitude, @Query("longitude") float longitude);

    @GET("/v1/estimates/price")
    Observable<UberPriceResponse> getEstimates(@Query("start_latitude") float startLat, @Query("start_longitude") float startLng, @Query("end_latitude") float endLat, @Query("end_longitude") float endLng);

    @GET("/v1/estimates/time")
    Observable<UberTimeResponse> getTimes(@Query("start_latitude") float startLat, @Query("start_longitude") float startLng);
}

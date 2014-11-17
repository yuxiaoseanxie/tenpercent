package com.livenation.mobile.android.na.uber.service;

import com.livenation.mobile.android.na.uber.service.model.UberPrices;
import com.livenation.mobile.android.na.uber.service.model.UberProducts;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by cchilton on 11/17/14.
 */
public interface UberService {

    @GET("/v1/products")
    public UberProducts getProducts(@Query("latitude") float latitude, @Query("longitude") float longitude);

    @GET("/v1/estimates/price")
    public UberPrices getEstimates(@Query("start_latitude") float startLat, @Query("start_longitude") float startLng, @Query("end_latitude") float endLat, @Query("end_longitude") float endLng);
}

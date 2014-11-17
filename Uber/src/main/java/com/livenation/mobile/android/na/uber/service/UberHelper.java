package com.livenation.mobile.android.na.uber.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.na.uber.BuildConfig;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.JacksonConverter;
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

}

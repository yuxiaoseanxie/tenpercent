package com.livenation.mobile.android.na.helpers;

import android.util.Log;

import com.livenation.mobile.android.na.apiconfig.ConfigManager;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.YenteResponse;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.OrderHistoryParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;
import com.livenation.mobile.android.ticketing.Ticketing;

import org.json.JSONObject;

public class OrderHistoryUploadHelper implements Ticketing.OrderHistoryUploadHandler {
    @Override
    public void uploadOrderHistoryBlob(final JSONObject blob) {
        getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                OrderHistoryParameters params = new OrderHistoryParameters();
                params.setOrderHistoryBlob(blob);
                apiService.uploadOrderHistory(params, new BasicApiCallback<YenteResponse>() {
                    @Override
                    public void onResponse(YenteResponse response) {
                        Log.i(getClass().getName(), "Successfully uploaded order history to platform.");
                    }

                    @Override
                    public void onErrorResponse(LiveNationError error) {
                        Log.e(getClass().getName(), "Could not upload order history to platform.", error);
                    }
                });
            }

            @Override
            public void onApiServiceNotAvailable() {

            }
        });
    }

    private ConfigManager getApiHelper() {
        return LiveNationApplication.get().getConfigManager();
    }
}

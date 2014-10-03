package com.livenation.mobile.android.na.providers;

import com.apsalar.sdk.Apsalar;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.AccessToken;
import com.livenation.mobile.android.platform.init.provider.AccessTokenProvider;

/**
 * Created by elodieferrais on 9/29/14.
 */
public class AccessTokenAppProvider extends AccessTokenProvider {
    @Override
    public void setAccessToken(AccessToken accessTokenValue) {
        super.setAccessToken(accessTokenValue);
        if (accessTokenValue != null) {
            //Apsalar
            Apsalar.event(AnalyticConstants.APSALAR_LN_LOGIN, AnalyticConstants.TOKEN, accessTokenValue.getToken(), AnalyticConstants.TOKEN_TYPE, accessTokenValue.getType());
        }
    }
}

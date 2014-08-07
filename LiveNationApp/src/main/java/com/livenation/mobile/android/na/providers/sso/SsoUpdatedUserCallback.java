package com.livenation.mobile.android.na.providers.sso;

import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

/**
 * Created by elodieferrais on 7/11/14.
 */
public interface SsoUpdatedUserCallback {
    void onResponse(boolean hasChanged, String accessToken, User user);

    void onErrorResponse(LiveNationError error);
}

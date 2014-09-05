package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;
import com.livenation.mobile.android.platform.sso.SsoManager;

public interface AccountUserView extends PresenterView {
    void setUser(User user, SsoManager.AuthConfiguration authConfiguration);
}

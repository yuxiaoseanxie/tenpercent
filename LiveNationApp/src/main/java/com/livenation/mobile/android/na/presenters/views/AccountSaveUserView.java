package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public interface AccountSaveUserView extends PresenterView {
    void onSaveUserSuccess(User user);

    void onSaveUserFailure();
}

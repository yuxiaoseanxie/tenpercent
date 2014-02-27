package com.livenation.mobile.android.na2.presenters.views;

import com.livenation.mobile.android.na2.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.User;

public interface AccountUserView extends PresenterView {
	void setUser(User user);
}

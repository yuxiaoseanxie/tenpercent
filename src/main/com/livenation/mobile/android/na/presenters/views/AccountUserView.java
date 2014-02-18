package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.models.User;
import com.livenation.mobile.android.na.presenters.support.PresenterView;

public interface AccountUserView extends PresenterView {
	void setUser(User user);
}

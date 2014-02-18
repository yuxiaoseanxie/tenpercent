package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.models.User;
import com.livenation.mobile.android.na.presenters.support.PresenterView;

public interface AccountSaveUserView extends PresenterView {
	void onSaveUserSuccess(User user);
	void onSaveUserFailure();
}

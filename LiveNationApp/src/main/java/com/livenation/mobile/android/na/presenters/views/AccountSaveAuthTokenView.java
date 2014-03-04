package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;

public interface AccountSaveAuthTokenView extends PresenterView {
	void onSaveAuthTokenSuccess();
	void onSaveAuthTokenFailure();
}

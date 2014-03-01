package com.livenation.mobile.android.na2.presenters.views;

import com.livenation.mobile.android.na2.presenters.support.PresenterView;

public interface AccountSaveAuthTokenView extends PresenterView {
	void onSaveAuthTokenSuccess();
	void onSaveAuthTokenFailure();
}

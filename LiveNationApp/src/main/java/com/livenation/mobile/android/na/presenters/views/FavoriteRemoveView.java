/*
 * 
 * @author Charlie Chilton 2014/02/25
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;

public interface FavoriteRemoveView extends PresenterView {
	void onFavoriteRemoveSuccess();
	void onFavoriteRemoveFailed();
}

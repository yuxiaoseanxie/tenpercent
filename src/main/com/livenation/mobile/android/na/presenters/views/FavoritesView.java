/*
 * 
 * @author Charlie Chilton 2014/02/25
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.views;

import java.util.List;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

public interface FavoritesView extends PresenterView {
	void setFavorites(List<Favorite> favorites);
}

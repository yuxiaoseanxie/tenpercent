/*
 * 
 * @author Charlie Chilton 2014/02/25
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

import java.util.List;

public interface FavoritesView extends PresenterView {
    void setFavorites(List<Favorite> favorites);
}

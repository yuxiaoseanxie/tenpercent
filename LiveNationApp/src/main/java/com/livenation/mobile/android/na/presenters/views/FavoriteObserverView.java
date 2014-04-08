package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

/**
 * Created by cchilton on 2/27/14.
 */
public interface FavoriteObserverView extends PresenterView {
    void onFavoriteAdded(Favorite favorite);

    void onFavoriteRemoved(Favorite favorite);
}

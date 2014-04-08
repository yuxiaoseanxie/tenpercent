package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

public interface SingleArtistView extends PresenterView {
    void setSingleArtist(Artist artist);
}

package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter.ArtistEvents;
import com.livenation.mobile.android.na.presenters.support.PresenterView;

public interface ArtistEventsView extends PresenterView {
    void setArtistEvents(ArtistEvents artistEvents);
}

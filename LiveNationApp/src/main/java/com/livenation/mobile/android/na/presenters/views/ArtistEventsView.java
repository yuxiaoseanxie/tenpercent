package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.ArtistEvents;

public interface ArtistEventsView extends PresenterView {
    void setArtistEvents(ArtistEvents artistEvents);
}

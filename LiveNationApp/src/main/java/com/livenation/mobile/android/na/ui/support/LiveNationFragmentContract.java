/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;

public interface LiveNationFragmentContract {

    ImageLoader getImageLoader();


    SingleArtistPresenter getSingleArtistPresenter();

    ArtistEventsPresenter getArtistEventsPresenter();

    EventsPresenter getEventsPresenter();

    AccountPresenters getAccountPresenters();

}

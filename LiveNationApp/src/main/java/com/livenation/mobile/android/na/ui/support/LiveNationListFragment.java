/*
 * 
 * @author Charlie Chilton 2014/01/17
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import android.os.Bundle;
import android.support.v4.app.ListFragment;

import com.android.volley.toolbox.ImageLoader;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.LocationManager;
import com.livenation.mobile.android.na.presenters.AccountPresenters;
import com.livenation.mobile.android.na.presenters.ArtistEventsPresenter;
import com.livenation.mobile.android.na.presenters.EventsPresenter;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.FeaturePresenter;
import com.livenation.mobile.android.na.presenters.NearbyVenuesPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationSetsPresenter;
import com.livenation.mobile.android.na.presenters.RecommendationsPresenter;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;

public abstract class LiveNationListFragment extends ListFragment implements LiveNationFragmentContract {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public ImageLoader getImageLoader() {
        return LiveNationApplication.get().getImageLoader();
    }

    @Override
    public EventsPresenter getEventsPresenter() {
        return LiveNationApplication.get().getEventsPresenter();
    }

    @Override
    public ArtistEventsPresenter getArtistEventsPresenter() {
        return LiveNationApplication.get().getArtistEventsPresenter();
    }

    @Override
    public SingleArtistPresenter getSingleArtistPresenter() {
        return LiveNationApplication.get().getSingleArtistPresenter();
    }

    @Override
    public FeaturePresenter getFeaturePresenter() {
        return LiveNationApplication.get().getFeaturePresenter();
    }

    @Override
    public NearbyVenuesPresenter getNearbyVenuesPresenter() {
        return LiveNationApplication.get().getNearbyVenuesPresenter();
    }

    @Override
    public FavoritesPresenter getFavoritesPresenter() {
        return LiveNationApplication.get().getFavoritesPresenter();
    }

    @Override
    public AccountPresenters getAccountPresenters() {
        return LiveNationApplication.get().getAccountPresenters();
    }

    @Override
    public SingleEventPresenter getSingleEventPresenter() {
        return LiveNationApplication.get().getSingleEventPresenter();
    }

    @Override
    public RecommendationsPresenter getRecommendationsPresenter() {
        return LiveNationApplication.get().getRecommendationsPresenter();
    }

    @Override
    public RecommendationSetsPresenter getRecommendationSetsPresenter() {
        return LiveNationApplication.get().getRecommendationSetsPresenter();
    }

}

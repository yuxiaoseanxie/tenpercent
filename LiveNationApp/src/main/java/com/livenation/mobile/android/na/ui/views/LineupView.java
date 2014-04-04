/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.FavoriteObserverPresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteObserverView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

public class LineupView extends LinearLayout {
	private CheckBox favorite;
	private TextView title;
	private FavoriteArtistObserver favoriteObserver;
    private FavoriteObserverPresenter observerPresenter = LiveNationApplication.get().getFavoritesPresenter().getObserverPresenter();

    public LineupView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public LineupView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LineupView(Context context) {
		super(context);
		init(context);
	}
	
	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);

		//TODO: Specifying this view as the rootview causes a stack overflow in the XML IDE
		//No biggy, but at the moment there's a redundant LinearLayout (PERFORMANCE!!)
		View view = inflater.inflate(R.layout.view_lineup, null);
		
		favorite = (CheckBox) view.findViewById(R.id.view_lineup_favorite_checkbox);
		title = (TextView) view.findViewById(R.id.view_lineup_title);

		addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        deinitObserver(favoriteObserver);
    }

    public TextView getTitle() {
		return title;
	}
	
	public CheckBox getFavorite() {
		return favorite;
	}

    public void setFavoriteObserver(Artist entry) {
        favoriteObserver = initObserver(entry, favorite);
    }

    private FavoriteArtistObserver initObserver(Artist lineup, CheckBox checkBox) {
        FavoriteArtistObserver observer = new FavoriteArtistObserver(favorite);

        Bundle args = observerPresenter.getBundleArgs(Favorite.FAVORITE_ARTIST, lineup.getNumericId());

        observerPresenter.initialize(getContext(), args, observer);

        return observer;
    }

    private void deinitObserver(FavoriteArtistObserver observer) {
       observerPresenter.cancel(observer);
    }

    private class FavoriteArtistObserver implements FavoriteObserverView {
        private final CheckBox checkBox;

        private FavoriteArtistObserver(CheckBox checkBox) {
            this.checkBox = checkBox;
        }

        @Override
        public void onFavoriteAdded(Favorite favorite) {
            checkBox.setChecked(true);
        }

        @Override
        public void onFavoriteRemoved(Favorite favorite) {
            checkBox.setChecked(false);
        }
    }
}

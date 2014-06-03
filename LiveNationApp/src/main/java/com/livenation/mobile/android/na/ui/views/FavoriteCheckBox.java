package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.widget.CheckBox;

import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.presenters.FavoritesPresenter;
import com.livenation.mobile.android.na.presenters.views.FavoriteObserverView;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

/**
 * Created by cchilton on 4/3/14.
 * <p/>
 * An extended CheckBox class that encapsulates the mapping a Favorite Checkbox's state to the User's favorites
 */
public class FavoriteCheckBox extends CheckBox implements FavoriteObserverView {
    private FavoritesPresenter favoritesPresenter;

    public FavoriteCheckBox(Context context) {
        super(context);
    }

    public FavoriteCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoriteCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stop();
    }

    public void bindToFavorite(int favoriteTypeId, String favoriteName, long itemId, FavoritesPresenter favoritesPresenter, AnalyticsCategory category) {
        this.favoritesPresenter = favoritesPresenter;
        stop();
        setChecked(false);

        Bundle args = favoritesPresenter.getObserverPresenter().getBundleArgs(favoriteTypeId, itemId);
        //Bind our checkbox to the FavoriteObserver so that it can update the checked state of this control
        //if the API Entity this checkbox represents is favorited, this line makes sure that we are Checked
        favoritesPresenter.getObserverPresenter().initialize(getContext(), args, this);

        Favorite favorite = new Favorite();
        favorite.setIntType(favoriteTypeId);
        favorite.setName(favoriteName);
        favorite.setId(itemId);

        //Set a clickListener that will update the user's favorites with the API if they check/uncheck
        //this checkbox
        OnFavoriteClickListener.OnFavoriteClick clickListener = new OnFavoriteClickListener.OnFavoriteClick(favorite, favoritesPresenter, getContext(), category);
        setOnClickListener(clickListener);
    }

    @Override
    public void onFavoriteAdded(Favorite favorite) {
        setChecked(true);
    }

    @Override
    public void onFavoriteRemoved(Favorite favorite) {
        setChecked(false);
    }

    /**
     * Cancel any bindings. this will prevent any in progress API requests from triggering
     * 'onFavoriteAdded' or 'onFavoriteRemoved'
     */
    private void stop() {
        if (null != favoritesPresenter) {
            //release the observer binding, prevent memory leaks
            favoritesPresenter.getObserverPresenter().cancel(FavoriteCheckBox.this);
        }
    }
}

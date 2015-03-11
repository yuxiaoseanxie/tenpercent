package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.support.OnFavoriteClickListener;
import com.livenation.mobile.android.platform.Constants;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.init.LiveNationLibrary;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.widget.CheckBox;

/**
 * Created by cchilton on 4/3/14.
 * <p/>
 * An extended CheckBox class that encapsulates the mapping a Favorite Checkbox's state to the User's favorites
 */
public class FavoriteCheckBox extends CheckBox {
    private Favorite favorite;
    private final Set<Favorite> favorites = Collections.synchronizedSet(new HashSet<Favorite>());

    private BroadcastReceiver updateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            favorites.clear();
            favorites.addAll(LiveNationLibrary.getFavoritesHelper().getFavorites());
            updateCheckBoxState();
        }
    };

    private void updateCheckBoxState() {
        if (favorite != null) {
            for (Favorite fav : favorites) {
                if (fav.idEquals(favorite)) {
                    if (!isChecked()) {
                        setChecked(true);
                    }
                    break;
                } else {
                    if (isChecked()) {
                        setChecked(false);
                    }
                }

            }
        }

    }

    public FavoriteCheckBox(Context context) {
        super(context);
    }

    public FavoriteCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoriteCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void bindToFavorite(Favorite favorite, AnalyticsCategory category) {
        this.favorite = favorite;
        favorites.clear();
        favorites.addAll(LiveNationLibrary.getFavoritesHelper().getFavorites());
        LocalBroadcastManager.getInstance(LiveNationApplication.get().getApplicationContext()).registerReceiver(updateBroadcastReceiver, new IntentFilter(Constants.FAVORITE_UPDATE_INTENT_FILTER));
        updateCheckBoxState();
        //Set a checkListener that will update the user's favorites with the API if they check/uncheck
        //this checkbox
        OnFavoriteClickListener.OnFavoriteClick checkListener = new OnFavoriteClickListener.OnFavoriteClick(favorite, category);
        setOnClickListener(checkListener);
    }
}

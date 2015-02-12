/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LineupView extends LinearLayout {
    private FavoriteCheckBox favorite;
    private TextView title;
    private View divider;

    public LineupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    public LineupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineupView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_lineup, this);

        favorite = (FavoriteCheckBox) findViewById(R.id.view_lineup_favorite_checkbox);
        title = (TextView) findViewById(R.id.view_lineup_title);
        divider = findViewById(R.id.view_lineup_divider);
    }

    public TextView getTitle() {
        return title;
    }

    public View getDivider() {
        return divider;
    }

    public void bindToFavoriteArtist(Artist artist) {
        favorite.bindToFavorite(Favorite.fromArtist(artist), AnalyticsCategory.SDP);
    }
}

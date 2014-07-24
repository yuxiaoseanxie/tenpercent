/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;

public class LineupView extends LinearLayout {
    private FavoriteCheckBox favorite;
    private TextView title;
    private View divider;

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

        favorite = (FavoriteCheckBox) view.findViewById(R.id.view_lineup_favorite_checkbox);
        title = (TextView) view.findViewById(R.id.view_lineup_title);
        divider = view.findViewById(R.id.view_lineup_divider);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public TextView getTitle() {
        return title;
    }

    public View getDivider() {
        return divider;
    }

    public void bindToFavoriteArtist(Artist artist) {
        Favorite fav = new Favorite();
        fav.setId(artist.getNumericId());
        fav.setIntType(Favorite.FAVORITE_ARTIST);
        fav.setName(artist.getName());
        favorite.bindToFavorite(fav, AnalyticsCategory.SDP);
    }
}

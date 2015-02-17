/*
 * 
 * @author Charlie Chilton 2014/01/28
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowVenueView extends LinearLayout {
    private FavoriteCheckBox favorite;
    private TextView title;
    private TextView telephone;
    private TextView location;

    public ShowVenueView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ShowVenueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShowVenueView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_show_venue_details, this);

        favorite = (FavoriteCheckBox) findViewById(R.id.view_show_venue_favorite_checkbox);
        title = (TextView) findViewById(R.id.view_show_venue_title);
        location = (TextView) findViewById(R.id.venue_detail_location);
        telephone = (TextView) findViewById(R.id.venue_detail_telephone);
    }

    public TextView getTitle() {
        return title;
    }

    public FavoriteCheckBox getFavorite() {
        return favorite;
    }

    public TextView getTelephone() {
        return telephone;
    }

    public TextView getLocation() {
        return location;
    }

}

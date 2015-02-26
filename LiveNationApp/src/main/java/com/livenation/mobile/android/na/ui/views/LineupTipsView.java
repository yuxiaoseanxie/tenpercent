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

/**
 * Created by elodieferrais on 2/25/15.
 */
public class LineupTipsView extends LinearLayout{

    private TextView time;
    private TextView title;
    private View divider;

    public LineupTipsView(Context context) {
        super(context);
        init();
    }

    public LineupTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineupTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_lineup_tips_cell, this);

        //time = (TextView) findViewById(R.id.view_lineup_time);
        //title = (TextView) findViewById(R.id.view_lineup_title);
        //divider = findViewById(R.id.view_lineup_divider);
    }

    public TextView getTitle() {
        return title;
    }

    public View getDivider() {
        return divider;
    }

    public TextView getTime() {
        return time;
    }
}

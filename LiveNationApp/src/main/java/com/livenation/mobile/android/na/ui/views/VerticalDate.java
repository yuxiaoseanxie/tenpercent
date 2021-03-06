/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.views;

import com.livenation.mobile.android.na.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VerticalDate extends LinearLayout {
    private final SimpleDateFormat DAY_FORMATTER = new SimpleDateFormat("d", Locale.getDefault());
    private final SimpleDateFormat DOTW_FORMATTER = new SimpleDateFormat("EEE", Locale.getDefault());
    private final SimpleDateFormat MONTH_FORMATTER = new SimpleDateFormat("MMM", Locale.getDefault());

    private TextView dateDotw;
    private TextView dateDay;
    private TextView dateMonth;

    public VerticalDate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public VerticalDate(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VerticalDate(Context context) {
        super(context);
        init(context);
    }

    public void setDate(Date date, TimeZone timeZone, boolean isMultiDay) {
        if (isMultiDay) {
            this.dateDotw.setText(getContext().getString(R.string.view_date_multiday));
            this.dateDay.setVisibility(View.GONE);
            this.dateMonth.setVisibility(View.GONE);
            return;
        }

        this.dateDay.setVisibility(View.VISIBLE);
        this.dateMonth.setVisibility(View.VISIBLE);

        DAY_FORMATTER.setTimeZone(timeZone);
        String day = DAY_FORMATTER.format(date);
        this.dateDay.setText(day);

        DOTW_FORMATTER.setTimeZone(timeZone);
        String dotw = DOTW_FORMATTER.format(date);
        this.dateDotw.setText(dotw);

        MONTH_FORMATTER.setTimeZone(timeZone);
        String month = MONTH_FORMATTER.format(date);
        this.dateMonth.setText(month);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(R.layout.view_vertical_date, isInEditMode() ? null : this, false);

        this.dateDotw = (TextView) view.findViewById(R.id.list_show_item_date_dotw);
        this.dateDay = (TextView) view.findViewById(R.id.list_show_item_date_day);
        this.dateMonth = (TextView) view.findViewById(R.id.list_show_item_date_month);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }


}

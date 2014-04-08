/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;

import java.util.Date;

public class VerticalDate extends LinearLayout {
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

    public void setDate(Date date) {
        String day = DateFormat.format("d", date).toString();
        String dotw = DateFormat.format("EEE", date).toString();
        String month = DateFormat.format("MMM", date).toString();

        this.dateDay.setText(day);
        this.dateDotw.setText(dotw);
        this.dateMonth.setText(month);
    }

    private void init(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);

        //TODO: Specifying this view as the rootview causes a stack overflow in the XML IDE
        //No biggy, but at the moment there's a redundant LinearLayout (PERFORMANCE!!)
        View view = inflater.inflate(R.layout.view_vertical_date, null);

        this.dateDotw = (TextView) view.findViewById(R.id.list_show_item_date_dotw);
        this.dateDay = (TextView) view.findViewById(R.id.list_show_item_date_day);
        this.dateMonth = (TextView) view.findViewById(R.id.list_show_item_date_month);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }


}

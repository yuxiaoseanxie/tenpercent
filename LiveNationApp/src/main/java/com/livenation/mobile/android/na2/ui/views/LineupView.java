/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na2.R;

public class LineupView extends LinearLayout {
	private CheckBox favorite;
	private TextView title;
	
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

	public TextView getTitle() {
		return title;
	}
	
	public CheckBox getFavorite() {
		return favorite;
	}
	
}

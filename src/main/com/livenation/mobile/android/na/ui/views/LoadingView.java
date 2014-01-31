package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.livenation.mobile.android.na.R;

public class LoadingView extends LinearLayout {

	public LoadingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public LoadingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public LoadingView(Context context) {
		super(context);
		init(context);
	}

	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);

		//TODO: Specifying this view as the rootview causes a stack overflow in the XML IDE
		//No biggy, but at the moment there's a redundant LinearLayout (PERFORMANCE!!)
		View view = inflater.inflate(R.layout.view_loading, null);
		
		
		addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	}
}

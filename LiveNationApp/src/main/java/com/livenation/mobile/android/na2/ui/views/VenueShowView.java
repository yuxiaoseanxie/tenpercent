package com.livenation.mobile.android.na2.ui.views;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.livenation.mobile.android.na2.R;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class VenueShowView extends LinearLayout {
	private TextView title;
	private TextView time;
	private VerticalDate date;
	//TODO: Move date parsing to Data Model Entity helper. This is ugly 
	private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);
	
	public VenueShowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	public VenueShowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public VenueShowView(Context context) {
		super(context);
		init(context);
	}
	
	public void setEvent(Event event) {
		title.setText(event.getName());
		Date start;
		try {
			start = getDate(event.getLocalStartTime());
			time.setText(getTimeText(start));
			date.setDate(start);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Invalid start time: " + event.getLocalStartTime());
		}
	}
	
	private Date getDate(String dateRaw) throws ParseException {
		Date date = DATE_FORMATTER.parse(dateRaw);
		return date;
	}
	
	private String getTimeText(Date date) {
		String timeValue = DateFormat.format("h:mm aa zzz", date).toString();
		return timeValue;
	}
	
	private void init(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);

		//TODO: Specifying this view as the rootview causes a stack overflow in the XML IDE
		//No biggy, but at the moment there's a redundant LinearLayout (PERFORMANCE!!)
		View view = inflater.inflate(R.layout.view_venue_show, null);
		
		title = (TextView) view.findViewById(R.id.view_venue_show_title);
		time = (TextView) view.findViewById(R.id.view_venue_show_time);
		date = (VerticalDate) view.findViewById(R.id.view_venue_show_date);
		
		addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}
}
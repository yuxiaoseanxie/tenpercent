package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ShowView extends LinearLayout {
    //TODO: Move date parsing to Data Model Entity helper. This is ugly
    private final static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(LiveNationApiService.LOCAL_START_TIME_FORMAT, Locale.US);

    private DisplayMode displayMode;

    private TextView title;
    private TextView details;
    private VerticalDate date;
    private NetworkImageView imageView;
    private View bottomLine;

    public ShowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ShowView(Context context) {
        super(context);
        init(context);
    }

    public void setEvent(Event event) {
        switch (getDisplayMode()) {
            case VENUE:
                title.setText(event.getName());
                break;

            case ARTIST:
                title.setText(event.getVenue().getName());
                break;

            case EVENT:
                title.setText(event.getDisplayName());
                break;
        }

        Date start;
        try {
            start = getDate(event.getLocalStartTime());
            date.setDate(start);

            switch (getDisplayMode()) {
                case VENUE:
                    details.setText(getTimeText(start));
                    break;

                case ARTIST:
                    //TODO: Spin this out
                    Venue venue = event.getVenue();
                    details.setText(String.format("%s, %s", venue.getAddress().getCity(), venue.getAddress().getState()));
                    break;

                case EVENT:
                    details.setText(event.getVenue().getName());
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid start time: " + event.getLocalStartTime());
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;

        if(displayMode != DisplayMode.EVENT)
            setImageVisible(false);
    }

    public boolean isBottomLineVisible() {
        return (bottomLine.getVisibility() == View.VISIBLE);
    }

    public void setBottomLineVisible(Boolean bottomLineVisible) {
        if (bottomLineVisible)
            bottomLine.setVisibility(View.VISIBLE);
        else
            bottomLine.setVisibility(View.GONE);
    }

    public boolean isImageVisible() {
        return (imageView.getVisibility() == View.VISIBLE);
    }

    public void setImageVisible(boolean imageVisible) {
        if(imageVisible)
            imageView.setVisibility(View.VISIBLE);
        else
            imageView.setVisibility(View.GONE);
    }

    public NetworkImageView getImageView() {
        return imageView;
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

        View view = inflater.inflate(R.layout.view_show, isInEditMode()? null : this, false);

        this.title = (TextView) view.findViewById(R.id.view_show_title);
        this.details = (TextView) view.findViewById(R.id.view_show_details);
        this.date = (VerticalDate) view.findViewById(R.id.view_show_date);
        this.imageView = (NetworkImageView) view.findViewById(R.id.view_show_image);
        this.bottomLine = view.findViewById(R.id.view_show_bottom_line);

        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        setDisplayMode(DisplayMode.VENUE);
    }


    public static enum DisplayMode {
        VENUE,
        ARTIST,
        EVENT,
    }
}
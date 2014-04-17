package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {
    private ShowView.DisplayMode displayMode;
    private boolean bottomLineVisible;

    public EventAdapter(Context context, ShowView.DisplayMode displayMode) {
        this(context, displayMode, new ArrayList<Event>());
    }

    public EventAdapter(Context context, ShowView.DisplayMode displayMode, List<Event> events) {
        super(context, android.R.layout.simple_list_item_1, events);

        this.displayMode = displayMode;
        this.bottomLineVisible = false;
    }

    //region Overrides

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShowView view = (ShowView) convertView;
        if (view == null) {
            view = new ShowView(getContext());
            view.setDisplayMode(getDisplayMode());
            view.setBottomLineVisible(isBottomLineVisible());
        }

        Event event = getItem(position);
        view.setEvent(event);

        return view;
    }

    //endregion


    //region Properties

    public ShowView.DisplayMode getDisplayMode() {
        return displayMode;
    }

    public boolean isBottomLineVisible() {
        return bottomLineVisible;
    }

    public void setBottomLineVisible(boolean bottomLineVisible) {
        this.bottomLineVisible = bottomLineVisible;
    }

    //endregion
}

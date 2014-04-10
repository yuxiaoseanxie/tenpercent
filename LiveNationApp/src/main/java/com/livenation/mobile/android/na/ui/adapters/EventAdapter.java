package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class EventAdapter extends ArrayAdapter<Event> {
    private ShowView.DisplayMode displayMode;

    public EventAdapter(Context context, ShowView.DisplayMode displayMode) {
        super(context, android.R.layout.simple_list_item_1);

        this.displayMode = displayMode;
    }

    //region Overrides

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ShowView view = (ShowView)convertView;
        if(view == null) {
            view = new ShowView(getContext());
            view.setDisplayMode(getDisplayMode());
        }

        Event event = getItem(position);
        view.setEvent(event);

        return view;
    }

    //endregion


    //region Getters

    public ShowView.DisplayMode getDisplayMode() {
        return displayMode;
    }


    //endregion
}

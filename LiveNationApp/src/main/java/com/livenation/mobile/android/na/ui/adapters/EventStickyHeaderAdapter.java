package com.livenation.mobile.android.na.ui.adapters;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class EventStickyHeaderAdapter extends EventAdapter implements StickyListHeadersAdapter {
    private LayoutInflater inflater;

    public EventStickyHeaderAdapter(Context context, ShowView.DisplayMode displayMode) {
        this(context, displayMode, new ArrayList<Event>());
    }

    public EventStickyHeaderAdapter(Context context, ShowView.DisplayMode displayMode, List<Event> events) {
        super(context, displayMode, events);

        this.inflater = LayoutInflater.from(context);
    }

    //region Headers

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        View view = null;
        ViewHeaderHolder holder = null;
        if (null == convertView) {
            view = inflater.inflate(R.layout.list_show_header, parent, false);
            holder = new ViewHeaderHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHeaderHolder) view.getTag();
        }

        TextView text = holder.getText();

        Date date = getItem(position).getLocalStartTime();
        String dateValue = DateFormat.format("MMMM", date).toString();
        text.setText(dateValue);

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        Date date = getItem(position).getLocalStartTime();
        String dateValue = DateFormat.format("yyyyMM", date).toString();
        return Long.valueOf(dateValue);
    }

    private class ViewHeaderHolder {
        private final TextView text;

        public ViewHeaderHolder(View view) {
            this.text = (TextView) view.findViewById(R.id.list_show_header_textview);
        }

        public TextView getText() {
            return text;
        }
    }

    //endregion
}

package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.SearchResult;

import java.util.List;

/**
 * Created by elodieferrais on 11/6/14.
 */
public abstract class SearchAdapter<ResultType> extends ArrayAdapter<ResultType> {
    private LayoutInflater inflater;
    private int textColor = android.R.color.black;

    public SearchAdapter(Context context, List<ResultType> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public ResultType getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (null == convertView) {
            view = inflater.inflate(getLayoutCellId(), parent, false);
        } else {
            view = convertView;
        }

        TextView title = (TextView) view.findViewById(getTitleTextViewId());

        if (title != null) {
            title.setTextColor(view.getResources().getColor(textColor));
        }

        return view;
    }

    public void startHighlightedMode() {
        this.textColor = android.R.color.darker_gray;
        notifyDataSetChanged();
    }

    public void stopHighlightedMode() {
        this.textColor = android.R.color.black;
    }

    protected abstract int getLayoutCellId();
    protected abstract int getTitleTextViewId();
}

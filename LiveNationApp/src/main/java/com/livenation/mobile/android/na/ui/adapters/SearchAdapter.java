package com.livenation.mobile.android.na.ui.adapters;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.viewcontroller.SearchViewHolder;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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

        if (convertView == null) {
            view = inflater.inflate(getLayoutCellId(), null);
            final SearchViewHolder viewHolder = new SearchViewHolder();
            view.setTag(viewHolder);
            viewHolder.title = (TextView) view.findViewById(R.id.list_search_title);
            viewHolder.type = (TextView) view.findViewById(R.id.list_search_type);
            viewHolder.checkBox = (FavoriteCheckBox) view.findViewById(R.id.list_search_checkbox);

        } else {
            view = convertView;
        }

        SearchViewHolder holder = (SearchViewHolder) view.getTag();

        if (holder.title != null) {
            holder.title.setTextColor(view.getResources().getColor(textColor));
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
}

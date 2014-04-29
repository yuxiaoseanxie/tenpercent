package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.ui.HelpMenuActivity;
import com.livenation.mobile.android.na.ui.fragments.HelpMenuFragment;

import java.util.List;

/**
 * Created by elodieferrais on 4/28/14.
 */
public class HelpListAdapter extends ArrayAdapter<HelpMenuFragment.HelpMenuItem> {
    public HelpListAdapter(Context context, List<HelpMenuFragment.HelpMenuItem> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView titleTv;
        TextView descriptionTv;

        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = mInflater.inflate(android.R.layout.two_line_list_item, parent, false);
        } else {
            view = convertView;
        }

        titleTv = (TextView) view.findViewById(android.R.id.text1);
        descriptionTv = (TextView) view.findViewById(android.R.id.text2);

        titleTv.setText(getItem(position).name);
        descriptionTv.setText(getItem(position).description);

        return view;
    }
}

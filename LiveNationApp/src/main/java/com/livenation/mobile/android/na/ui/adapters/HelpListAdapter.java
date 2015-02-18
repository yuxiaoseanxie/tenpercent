package com.livenation.mobile.android.na.ui.adapters;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.fragments.HelpMenuFragment;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

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
            view = mInflater.inflate(R.layout.help_menu_item, parent, false);
        } else {
            view = convertView;
        }

        titleTv = (TextView) view.findViewById(R.id.help_menu_item_name);
        descriptionTv = (TextView) view.findViewById(R.id.help_menu_item_description);

        titleTv.setText(getItem(position).name);
        descriptionTv.setText(getItem(position).description);

        return view;
    }
}

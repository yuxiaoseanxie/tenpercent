package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/23/15.
 */
public class LineUpFragment extends Fragment {
    private List<Artist> artists;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lineup, container, false);

        TextView header = (TextView) view.findViewById(R.id.lineup_header);
        header.setText(R.string.lineup_header);

        ListView listView = (ListView) view.findViewById(R.id.lineup_listview);
        ArrayAdapter adapter = new LineUpAdapter(getActivity(), artists);
        listView.setAdapter(adapter);
        return view;
    }

    public void setArtists(List<Artist> artists) {
        //Should be call before onCreateView
        this.artists = artists;
    }

    private class LineUpAdapter extends ArrayAdapter {

        public LineUpAdapter(Context context, List objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem viewHolder;

            if (convertView == null) {

                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.list_lineup_cell, parent, false);

                viewHolder = new ViewHolderItem();
                viewHolder.time = (TextView) convertView.findViewById(R.id.view_lineup_time);
                viewHolder.title = (TextView) convertView.findViewById(R.id.view_lineup_title);
                viewHolder.divider = convertView.findViewById(R.id.view_lineup_divider);

                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            viewHolder.time.setText("8:00");
            viewHolder.title.setText("Abdc Efghijklmnop");

            if (position == getCount() - 1) {
                viewHolder.divider.setVisibility(View.GONE);
            } else if (position == 0) {
                viewHolder.divider.setBackgroundDrawable(getResources().getDrawable(R.drawable.dotted_gray));
                viewHolder.divider.setVisibility(View.VISIBLE);
            } else {
                viewHolder.divider.setBackgroundColor(getResources().getColor(R.color.underscore));
                viewHolder.divider.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        @Override
        public int getCount() {
            return 3;
        }

        private class ViewHolderItem {
            public TextView title;
            public TextView time;
            public View divider;
        }
    }

}

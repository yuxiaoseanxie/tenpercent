package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.views.LineupTipsView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.EventTips;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.TipsTime;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Created by elodieferrais on 2/23/15.
 */
public class LineUpTipsFragment extends Fragment {
    private static final String TIME_FORMAT = "h:mm";
    private SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
    private static final String EVENTTIPS = "com.livenation.mobile.android.na.ui.fragments.LineUpFragment.EVENTTIPS";
    private EventTips eventTips;
    private ViewGroup lineupContainer;

    public static LineUpTipsFragment newInstance(EventTips eventTips) {
        LineUpTipsFragment fragment = new LineUpTipsFragment();
        Bundle args = new Bundle();
        args.putSerializable(EVENTTIPS, eventTips);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lineup, container, false);

        final TextView header = (TextView) view.findViewById(R.id.lineup_header);
        lineupContainer = (ViewGroup) view.findViewById(R.id.lineup_container);

        header.setText(R.string.lineup_header);
        EventTips eventTips = (EventTips) getArguments().getSerializable(EVENTTIPS);
        //TODO display a loader
        populateList(eventTips.getSetTimes());

        return view;
    }

    private void populateList(List<TipsTime> setTimes) {
        lineupContainer.removeAllViews();
        for (TipsTime lineup : setTimes) {
            LineupTipsView view = new LineupTipsView(getActivity());
            view.getTitle().setText(lineup.getName());
            view.getTime().setText(timeFormat.format(lineup.getStartTimeUtc()));
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
            lineupContainer.addView(view, layoutParams);

            int position = setTimes.indexOf(lineup);
            if (position == setTimes.size() - 1) {
                view.getDivider().setVisibility(View.GONE);
            } else if (position == 0) {
                view.getDivider().setBackgroundDrawable(getResources().getDrawable(R.drawable.dotted_gray));
                view.getDivider().setVisibility(View.VISIBLE);
            } else {
                view.getDivider().setBackgroundColor(getResources().getColor(R.color.underscore));
                view.getDivider().setVisibility(View.VISIBLE);
            }
        }
    }
}

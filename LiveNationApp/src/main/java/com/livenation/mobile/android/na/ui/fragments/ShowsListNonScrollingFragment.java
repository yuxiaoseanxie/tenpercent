package com.livenation.mobile.android.na.ui.fragments;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.segment.android.models.Props;

import java.util.List;

public class ShowsListNonScrollingFragment extends LiveNationFragment implements EventsView {
    public static final int MAX_EVENTS_INFINITE = Integer.MAX_VALUE;

    private int dividerHeight;
    private int dividerLeftMargin;
    private Drawable dividerBackground;

    private ShowView.DisplayMode displayMode;
    private ViewGroup showContainer;

    private int maxEvents;
    private View showMoreItemsView;
    private boolean alwaysShowMoreItemsView;
    private AnalyticsCategory category;

    //region Lifecycle

    public ShowsListNonScrollingFragment() {
        super();

        this.displayMode = ShowView.DisplayMode.VENUE;
        this.maxEvents = MAX_EVENTS_INFINITE;
    }

    public static ShowsListNonScrollingFragment newInstance(ShowView.DisplayMode displayMode, AnalyticsCategory category) {
        ShowsListNonScrollingFragment instance = new ShowsListNonScrollingFragment();
        instance.setDisplayMode(displayMode);
        instance.setCategory(category);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.dividerHeight = (int) getResources().getDimension(R.dimen.one_dp);
        this.dividerLeftMargin = (int) getResources().getDimension(R.dimen.ui_gutter_width);
        this.dividerBackground = getResources().getDrawable(R.drawable.ui_underscore_background);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_show_fixed, container, false);

        showContainer = (ViewGroup) result;

        return result;
    }

    //endregion


    @SuppressWarnings("deprecation")
    protected View createDivider() {
        View view = new View(getActivity());
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackgroundDrawable(dividerBackground);
        } else {
            view.setBackground(dividerBackground);
        }
        return view;
    }

    protected void addNewDivider() {
        View divider = createDivider();

        LayoutParams dividerLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, dividerHeight);
        dividerLayoutParams.setMargins(dividerLeftMargin, 0, 0, 0);

        showContainer.addView(divider, dividerLayoutParams);
    }

    public void setCategory(AnalyticsCategory category) {
        this.category = category;
    }

    @Override
    public void setEvents(List<Event> events) {
        showContainer.removeAllViews();

        int position = 0;
        int lastPositionWithDivider = events.size() - 1;
        for (Event event : events) {
            ShowView show = new ShowView(showContainer.getContext());
            show.setDisplayMode(getDisplayMode());
            show.setEvent(event);
            show.setOnClickListener(new ShowViewClickListener(event));

            LayoutParams showLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            showContainer.addView(show, showLayoutParams);

            position++;
            if (position >= getMaxEvents())
                break;
            else if (position <= lastPositionWithDivider)
                addNewDivider();
        }

        if ((events.size() > getMaxEvents() || alwaysShowMoreItemsView()) && getShowMoreItemsView() != null) {
            addNewDivider();

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            showContainer.addView(getShowMoreItemsView(), layoutParams);
        }

        if (events.size() == 0) {
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            showContainer.addView(getEmptyView(), layoutParams);
        }
    }


    //region Properties

    public ShowView.DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(ShowView.DisplayMode displayMode) {
        this.displayMode = displayMode;
    }


    public int getMaxEvents() {
        return maxEvents;
    }

    public void setMaxEvents(int maxEvents) {
        this.maxEvents = maxEvents;
    }


    public View getShowMoreItemsView() {
        return showMoreItemsView;
    }

    public void setShowMoreItemsView(View showMoreItemsView) {
        this.showMoreItemsView = showMoreItemsView;
    }

    private View getEmptyView() {
        View emptyView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null);
        TextView tv = (TextView) emptyView.findViewById(android.R.id.text1);
        tv.setText(R.string.artist_events_no_show);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(emptyView.getResources().getColor(android.R.color.black));
        return emptyView;
    }

    public boolean alwaysShowMoreItemsView() {
        return alwaysShowMoreItemsView;
    }

    public void setAlwaysShowMoreItemsView(boolean alwaysShowMoreItemsView) {
        this.alwaysShowMoreItemsView = alwaysShowMoreItemsView;
    }

    //endregion


    private class ShowViewClickListener implements View.OnClickListener {
        private Event event;

        public ShowViewClickListener(Event event) {
            this.event = event;
        }

        @Override
        public void onClick(View view) {
            //Analytics
            Props props = AnalyticsHelper.getPropsForEvent(event);
            LiveNationAnalytics.track(AnalyticConstants.EVENT_CELL_TAP, category, props);

            Intent intent = new Intent(getActivity(), ShowActivity.class);

            Bundle args = ShowActivity.getArguments(event);
            intent.putExtras(args);

            startActivity(intent);
        }
    }
}

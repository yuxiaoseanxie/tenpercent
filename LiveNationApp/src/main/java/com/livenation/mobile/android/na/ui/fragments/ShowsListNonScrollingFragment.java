package com.livenation.mobile.android.na.ui.fragments;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.helpers.AnalyticsHelper;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

import io.segment.android.models.Props;

public class ShowsListNonScrollingFragment extends LiveNationFragment implements EventsView {
    public static final int MAX_EVENTS_INFINITE = Integer.MAX_VALUE;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_show_fixed, container,
                false);

        showContainer = (ViewGroup) result;

        return result;
    }

    //endregion


    public void setCategory(AnalyticsCategory category) {
        this.category = category;
    }

    @Override
    public void setEvents(List<Event> events) {
        showContainer.removeAllViews();

        int position = 0;
        for (Event event : events) {
            ShowView show = new ShowView(getActivity());
            show.setDisplayMode(getDisplayMode());
            show.setEvent(event);
            show.setOnClickListener(new ShowViewClickListener(event));

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            showContainer.addView(show, layoutParams);

            position++;
            if (position >= getMaxEvents())
                break;
        }

        if ((events.size() > getMaxEvents() || alwaysShowMoreItemsView()) && getShowMoreItemsView() != null) {
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


    private View getShowMoreItemsView() {
        return showMoreItemsView;
    }

    private View getEmptyView() {
        View emptyView =  LayoutInflater.from(getActivity().getApplicationContext()).inflate(android.R.layout.simple_list_item_1, null);
        TextView tv = (TextView) emptyView.findViewById(android.R.id.text1);
        tv.setText(R.string.artist_events_no_show);
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(emptyView.getResources().getColor(android.R.color.black));
        return emptyView;
    }

    public void setShowMoreItemsView(View showMoreItemsView) {
        this.showMoreItemsView = showMoreItemsView;
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

            Bundle args = SingleEventPresenter.getAruguments(event.getId());
            SingleEventPresenter.embedResult(args, event);
            intent.putExtras(args);

            startActivity(intent);
        }
    }
}

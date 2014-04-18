package com.livenation.mobile.android.na.ui.fragments;

import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.ShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

public class ShowsListNonScrollingFragment extends LiveNationFragment implements EventsView {
    public static final int MAX_EVENTS_INFINITE = Integer.MAX_VALUE;

    private ShowView.DisplayMode displayMode;
    private ViewGroup showContainer;

    private int maxEvents;
    private View showMoreItemsView;
    private boolean alwaysShowMoreItemsView;

    //region Lifecycle

    public ShowsListNonScrollingFragment() {
        super();

        this.displayMode = ShowView.DisplayMode.VENUE;
        this.maxEvents = MAX_EVENTS_INFINITE;
    }

    public static ShowsListNonScrollingFragment newInstance(ShowView.DisplayMode displayMode) {
        ShowsListNonScrollingFragment instance = new ShowsListNonScrollingFragment();
        instance.setDisplayMode(displayMode);
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
            Intent intent = new Intent(getActivity(), ShowActivity.class);

            Bundle args = SingleEventPresenter.getAruguments(event.getId());
            SingleEventPresenter.embedResult(args, event);
            intent.putExtras(args);

            startActivity(intent);
        }
    }
}

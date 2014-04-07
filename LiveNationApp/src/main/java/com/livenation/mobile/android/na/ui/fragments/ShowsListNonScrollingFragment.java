package com.livenation.mobile.android.na.ui.fragments;

import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.DetailShowView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

public class ShowsListNonScrollingFragment extends LiveNationFragment implements EventsView {
    public static final int MAX_EVENTS_INFINITE = Integer.MAX_VALUE;

    private DetailShowView.DisplayMode displayMode;
	private ViewGroup showContainer;

    private int maxEvents;
    private View showMoreItemsView;
    private boolean alwaysShowMoreItemsView;

    //region Lifecycle

    public static ShowsListNonScrollingFragment newInstance(DetailShowView.DisplayMode displayMode) {
        ShowsListNonScrollingFragment instance = new ShowsListNonScrollingFragment();
        instance.setDisplayMode(displayMode);
        return instance;
    }

    public ShowsListNonScrollingFragment() {
        super();

        this.displayMode = DetailShowView.DisplayMode.VENUE;
        this.maxEvents = MAX_EVENTS_INFINITE;
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
			DetailShowView show = new DetailShowView(getActivity());
            show.setDisplayMode(getDisplayMode());
			show.setEvent(event);

			LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			showContainer.addView(show, layoutParams);

            position++;
            if(position >= getMaxEvents())
                break;
		}

        if((events.size() > getMaxEvents() || alwaysShowMoreItemsView()) && getShowMoreItemsView() != null) {
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            showContainer.addView(getShowMoreItemsView(), layoutParams);
        }
	}


    //region Properties

    public DetailShowView.DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DetailShowView.DisplayMode displayMode) {
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
}

package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.pagination.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

/**
 * Created by elodieferrais on 5/28/14.
 */
public abstract class LiveNationFragmentTab extends LiveNationFragment implements SwipeRefreshLayout.OnRefreshListener {
    protected StickyListHeadersListView listView;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected BaseDecoratedScrollPager scrollPager;
    protected EmptyListViewControl emptyListViewControl;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState, int res) {
        View view = inflater.inflate(res, container, false);

        listView = (StickyListHeadersListView) view.findViewById(R.id.fragment_tab_list);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.fragment_tab_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorScheme(R.color.refresh_color_1, R.color.refresh_color_2, R.color.refresh_color_3, R.color.refresh_color_4);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        scrollPager.setEmptyView(emptyListViewControl);
        listView.setEmptyView(emptyListViewControl);

        getScrollPager().connectSwipeRefreshLayout(swipeRefreshLayout);
        getScrollPager().connectListView(listView);

        //Scroll until the top of the list. Refresh only when the first item of the listview is visible.
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrollPager.onScrollStateChanged(view, scrollState);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                scrollPager.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
                if (listView.getChildCount() > 0) {
                    ViewGroup viewGroup = (ViewGroup) listView.getChildAt(0);
                    if (viewGroup.getChildCount() > 0) {
                        int top = viewGroup.getChildAt(0).getTop();
                        swipeRefreshLayout.setEnabled(top == 0);
                    }
                }
            }
        });

        return view;
    }

    abstract BaseDecoratedScrollPager getScrollPager();

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setSoundEffectsEnabled(true);
        getScrollPager().resetData();
        getScrollPager().load();
    }


}

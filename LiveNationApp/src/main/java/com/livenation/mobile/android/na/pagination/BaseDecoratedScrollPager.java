package com.livenation.mobile.android.na.pagination;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.livenation.mobile.android.na.ui.viewcontroller.RefreshBarController;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemTypeOutput extends IdEquals<TItemTypeOutput>, TItemTypeInput> extends BaseScrollPager<TItemTypeOutput> {
    protected static final int DEFAULT_LIMIT = 10;
    private final EmptyListViewControl listLoadingView;
    /*
    Use a frame layout to contain our loading view. This is necessary since Android doesn't like direct
    modification to the footview's view, and will leave its whitespace visible even if the view is
    set to View.GONE :-/
    http://stackoverflow.com/questions/4317778/hide-footer-view-in-listview?rq=1
    http://stackoverflow.com/questions/7576099/hiding-footer-in-listview
     */
    private final ViewGroup footerBugHack;
    protected EmptyListViewControl emptyView;
    private RefreshBarController refreshBarController;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View.OnClickListener retryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            load();
        }
    };

    protected BaseDecoratedScrollPager(int limit, ArrayAdapter<TItemTypeOutput> adapter) {
        super(limit, adapter);

        Context context = adapter.getContext();
        listLoadingView = new EmptyListViewControl(context);
        listLoadingView.setRetryOnClickListener(retryClickListener);
        footerBugHack = new FrameLayout(context);
        footerBugHack.addView(listLoadingView);
    }

    public void connectListView(StickyListHeadersListView listView) {
        if (listView.getAdapter() != null) {
            // http://stackoverflow.com/questions/4317778/hide-footer-view-in-listview?rq=1
            throw new IllegalStateException("Setting the adapter before the adding a footer is broken on many flavours of Android");
        }
        listView.setOnScrollListener(this);
        listView.addFooterView(footerBugHack);
    }

    public void connectListView(ListView listView) {
        listView.setOnScrollListener(this);
        listView.addFooterView(footerBugHack);
    }

    public void connectSwipeRefreshLayout(SwipeRefreshLayout swipeRefreshLayout) {
        this.swipeRefreshLayout = swipeRefreshLayout;
    }

    @Override
    public void onFetchStarted() {
        listLoadingView.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        if (null != emptyView) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        }
    }

    @Override
    public void onFetchEnded(boolean cancelled) {
        listLoadingView.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);

        if (getAdapter().getCount() == 0 && emptyView != null) {
            listLoadingView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
            emptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onFetchError() {
        listLoadingView.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);
        if (!isFirstPage && refreshBarController != null) {
            refreshBarController.showRefreshBar(false);
        } else {
            if (isFirstPage) {
                if (emptyView != null) {
                    emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                }
            }
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void setEmptyView(final EmptyListViewControl emptyView) {
        this.emptyView = emptyView;
        this.emptyView.setRetryOnClickListener(retryClickListener);
    }

    public void setRefreshBarView(RefreshBar refreshBar) {
        refreshBarController = new RefreshBarController(refreshBar, retryClickListener);
    }
}

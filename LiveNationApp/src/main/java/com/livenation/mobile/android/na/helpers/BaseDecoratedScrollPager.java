package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemType extends IdEquals<TItemType>> extends BaseScrollPager<TItemType> {
    private final View listLoadingView;
    /*
    Use a frame layout to contain our loading view. This is necessary since Android doesn't like direct
    modification to the footview's view, and will leave its whitespace visible even if the view is
    set to View.GONE :-/
    http://stackoverflow.com/questions/4317778/hide-footer-view-in-listview?rq=1
    http://stackoverflow.com/questions/7576099/hiding-footer-in-listview
     */
    private final ViewGroup footerBugHack;
    private static final int DEFAULT_LIMIT = 10;

    protected BaseDecoratedScrollPager(ArrayAdapter<TItemType> adapter) {
        super(DEFAULT_LIMIT, adapter);

        Context context = adapter.getContext();
        listLoadingView = new EmptyListViewControl(context);
        footerBugHack = new FrameLayout(context);
    }

    protected BaseDecoratedScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        super(limit, adapter);

        Context context = adapter.getContext();
        listLoadingView = new EmptyListViewControl(context);
        footerBugHack = new FrameLayout(context);
    }

    public void connectListView(StickyListHeadersListView listView) {
        listView.setOnScrollListener(this);
        listView.addFooterView(footerBugHack);
    }

    public void connectListView(ListView listView) {
        listView.setOnScrollListener(this);
        listView.addFooterView(footerBugHack);
    }

    @Override
    public void onFetchStarted() {
        footerBugHack.addView(listLoadingView);
    }

    @Override
    public void onFetchEnded() {
        footerBugHack.removeAllViews();
    }

    @Override
    public void onFetchError() {
        footerBugHack.removeAllViews();
        //TODO find a way to notify the user an error occurred
    }
}

package com.livenation.mobile.android.na.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.livenation.mobile.android.na.R;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemType> extends BaseScrollPager<TItemType> {
    private View listLoadingView;

    protected BaseDecoratedScrollPager(StickyListHeadersListView listView, int limit, ArrayAdapter<TItemType> adapter) {
        super(limit, adapter);

        listView.setOnScrollListener(this);

        LayoutInflater inflater = LayoutInflater.from(listView.getContext());
        listLoadingView = inflater.inflate(R.layout.view_loading, null);
        listLoadingView.setVisibility(View.GONE);

        listView.addFooterView(listLoadingView);

    }

    @Override
    public void onFetchStarted() {
        listLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFetchEnded() {
        listLoadingView.setVisibility(View.GONE);
    }
}

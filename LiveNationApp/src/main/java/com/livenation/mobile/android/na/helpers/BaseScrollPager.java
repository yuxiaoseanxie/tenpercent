package com.livenation.mobile.android.na.helpers;

import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.platform.util.Logger;

import java.util.List;

/**
 * Created by cchilton on 3/11/14.
 */
public abstract class BaseScrollPager<TItemType> implements AbsListView.OnScrollListener {
    private Loader loading;
    //limit is final due to the way the API offset parameter behaves as a page
    //if the limit changes from request to request, the api paging data will be inconsistent
    private final int limit;
    private final ArrayAdapter<TItemType> adapter;

    protected BaseScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        this.adapter = adapter;
        this.limit = limit;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {}

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!(isLoading()) && (totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
            load();
        }
    }

    public void load() {
        loading = new Loader(adapter.getCount(), limit);
        loading.run();
        onFetchStarted();
    }

    public void stop() {};

    private boolean isLoading() {
        return null != loading;
    }

    public abstract void fetch(int offset, int limit);

    public abstract void onFetchStarted();

    public abstract void onFetchEnded();

    public void onFetchResult(List<? extends TItemType> result) {
        loading = null;
        onFetchEnded();
        adapter.addAll(result);
        adapter.notifyDataSetChanged();
    }

    private class Loader implements Runnable {
        private final int offset;
        private final int limit;

        private Loader(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public void run() {
            fetch(offset, limit);
        }
    }
}

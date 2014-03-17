package com.livenation.mobile.android.na.helpers;

import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import java.util.List;

/**
 * Created by cchilton on 3/11/14.
 */
public abstract class BaseScrollPager<TItemType> implements AbsListView.OnScrollListener {
    private Loader loader;
    //limit is final due to the way the API offset parameter behaves as a page
    //if the limit changes from request to request, the api paging data will be inconsistent
    private final int limit;
    private final ArrayAdapter<TItemType> adapter;
    private boolean hasMorePages;

    protected BaseScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        this.adapter = adapter;
        this.limit = limit;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {}

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (!(isLoading()) && (totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
            if ((firstVisibleItem == 0) && (visibleItemCount <= totalItemCount)) {
                //StickyListViewHeaders bug? (the adapter has no items, yet totalItemCount is 1)
                //ignore this scroll based load request, as the adapter has just had its items
                //cleared, and the scroll pager will only start a duplicate non-user-scroll based request
                return;
            }
            load();
        }
    }

    public void reset() {
        if (null != loader) {
            loader.cancel();
            loader = null;
        }
        adapter.clear();
        setHasMorePages(true);
    }

    public void load() {
        if (isLoading()) {
            loader.cancel();
        }
        loader = new Loader(getOffset(), limit);
        loader.run();
        onFetchStarted();
    }

    private boolean isLoading() {
        return null != loader;
    }

    protected int getOffset() {
        return adapter.getCount();
    }

    public void onNoMorePages() {
        onFetchEnded();
    };

    public abstract void fetch(int offset, int limit);


    public abstract void onFetchStarted();

    public abstract void onFetchEnded();

    public void onFetchResult(List<? extends TItemType> result) {
        if (null != loader && loader.isCancelled()) {
            loader = null;
            return;
        }

        adapter.addAll(result);
        loader = null;

        onFetchEnded();
    }

    protected void setHasMorePages(boolean value) {
        hasMorePages = value;
    }

    private class Loader implements Runnable {
        private final int offset;
        private final int limit;
        private boolean cancelled = false;

        private Loader(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public void run() {
            if (hasMorePages) {
                fetch(offset, limit);
            } else {
                onNoMorePages();
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            cancelled = true;
            onFetchEnded();
         }
    }
}

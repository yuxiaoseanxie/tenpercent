package com.livenation.mobile.android.na.pagination;

import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

/**
 * Created by cchilton on 3/11/14.
 */
public abstract class BaseScrollPager<TItemType extends IdEquals<TItemType>> implements AbsListView.OnScrollListener {
    private final int limit;
    private final ArrayAdapter<TItemType> adapter;
    protected boolean isFirstPage = true;
    private PaginatedFetcher paginatedFetcher = null;
    private boolean hasMorePages = true;
    private List<TItemType> lastFetch;

    protected BaseScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        this.adapter = adapter;
        this.limit = limit;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (getAdapter().getCount() == 0) {
            //onScroll will be triggered when adapter.clear() is invoked.
            //we need to guard against on scrolling triggering a duplicate/unnecessary reload on this occasion,
            //as a separate load() call should have been invoked.
            return;
        }

        if ((totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
            load();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void resetDataAndClearView() {
        resetData();
        adapter.clear();

    }

    public void resetData() {
        stop();
        lastFetch = null;
        isFirstPage = true;
        setHasMorePages(true);
    }

    public void load() {
        if (paginatedFetcher != null) {
            return;
        }

        if (!hasMorePages) {
            onNoMorePages();
            return;
        }

        //Create a new fetcherLoader
        paginatedFetcher = new PaginatedFetcher(getOffset(), limit);
        onFetchStarted();
        paginatedFetcher.run();
    }

    public void stop() {
        if (paginatedFetcher != null) {
            paginatedFetcher.cancel();
            paginatedFetcher = null;
        }
    }

    protected int getOffset() {
        return adapter.getCount();
    }

    public void onNoMorePages() {
        onFetchEnded(true);
        paginatedFetcher = null;
    }

    protected void onFetchResult(List<TItemType> result) {
        if (isFirstPage) {
            adapter.clear();
        }
        if (result.size() == 0 || hasItemAlreadyBeenFetched(result)) {
            //end of result list, or
            //found a dupe, abort adding the data to the adapter
            setHasMorePages(false);
            onNoMorePages();
            return;
        }
        lastFetch = result;

        if (isFirstPage) {
            isFirstPage = false;
        }

        adapter.addAll(result);
        paginatedFetcher = null;
        onFetchEnded(false);
    }

    protected void onFetchCancelled() {
        paginatedFetcher = null;
        onFetchEnded(true);
    }

    protected void onFetchFailed() {
        paginatedFetcher = null;
        onFetchError();
    }

    public ArrayAdapter<TItemType> getAdapter() {
        return adapter;
    }

    private boolean hasItemAlreadyBeenFetched(List<? extends TItemType> newFetch) {
        if (null == lastFetch) return false;
        if (newFetch.size() == 0) return false;

        //The LN API will 'rollover' to the last page of results once paging parameters
        //are exceeded. So we need to check for dupes and stop paging if so.
        TItemType first = newFetch.get(0);
        for (TItemType existing : lastFetch) {
            if (first.idEquals(existing)) {
                return true;
            }
        }
        return false;
    }

    private void setHasMorePages(boolean value) {
        hasMorePages = value;
    }

    //Abstract methods

    protected abstract void fetch(int offset, int limit, BasicApiCallback<List<TItemType>> callback);

    public abstract void onFetchStarted();

    public abstract void onFetchEnded(boolean cancelled);

    public abstract void onFetchError();

    //Paginated Fetcher

    private class PaginatedFetcher implements Runnable, BasicApiCallback<List<TItemType>> {
        private final int offset;
        private final int limit;
        private boolean isCanceled = false;

        private PaginatedFetcher(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        @Override
        public void run() {
            if (hasMorePages) {
                fetch(offset, limit, this);
            } else {
                onNoMorePages();
            }
        }

        @Override
        public void onResponse(List<TItemType> response) {
            if (!isCanceled()) {
                onFetchResult(response);
            }
        }

        @Override
        public void onErrorResponse(LiveNationError error) {
            if (!isCanceled()) {
                onFetchFailed();
            }
        }

        public void cancel() {
            isCanceled = true;
            onFetchCancelled();
        }

        public boolean isCanceled() {
            return isCanceled;
        }
    }
}

package com.livenation.mobile.android.na.pagination;

import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import java.util.List;

/**
 * Created by cchilton on 3/11/14.
 */
public abstract class BaseScrollPager<TItemType extends IdEquals<TItemType>> implements AbsListView.OnScrollListener {
    private PaginatedFetcher paginatedFetcher = null;
    private final int limit;
    private final ArrayAdapter<TItemType> adapter;
    private boolean hasMorePages = true;
    private List<TItemType> lastFetch;
    private boolean isFirstPage = false;

    protected BaseScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        this.adapter = adapter;
        this.limit = limit;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if ((paginatedFetcher == null) && (totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
            load();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void reset() {
        lastFetch = null;
        isFirstPage = true;
        if (paginatedFetcher != null) {
            paginatedFetcher.cancel();
            paginatedFetcher = null;
        }
        setHasMorePages(true);
    }

    public void load() {

        //Clear pending fetcherLoader
        if (paginatedFetcher != null) {
            paginatedFetcher.cancel();
            paginatedFetcher = null;
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
        onFetchEnded();
    }

    protected void onFetchResult(List<TItemType> result) {
        if (result.size() == 0 || hasItemAlreadyBeenFetched(result)) {
            //end of result list, or
            //found a dupe, abort adding the data to the adapter
            setHasMorePages(false);
            onNoMorePages();
            return;
        }
        lastFetch = result;

        //Clear the adapter here insteadof the reset method to avoid the few seconds with a blank page during the loading
        if (isFirstPage) {
            adapter.clear();
        }
        isFirstPage = false;

        adapter.addAll(result);
        onFetchEnded();
        paginatedFetcher = null;
    }

    protected void onFetchCancelled() {
        onFetchEnded();
        paginatedFetcher = null;
    }

    protected void onFetchFailed() {
        onFetchError();
        paginatedFetcher = null;
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

    public abstract void fetch(int offset, int limit, ApiService.BasicApiCallback<List<TItemType>> callback);

    public abstract void onFetchStarted();

    public abstract void onFetchEnded();

    public abstract void onFetchError();

    //Paginated Fetcher

    private class PaginatedFetcher implements Runnable {
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
                fetch(offset, limit, new ApiService.BasicApiCallback<List<TItemType>>() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (!isCanceled()) {
                            onFetchFailed();
                        }
                    }

                    @Override
                    public void onResponse(List<TItemType> response) {
                        if (!isCanceled()) {
                            onFetchResult(response);
                        }
                    }
                });
            } else {
                onNoMorePages();
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

package com.livenation.mobile.android.na.pagination;

import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import com.android.volley.VolleyError;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/11/14.
 */
public abstract class BaseScrollPager<TItemType extends IdEquals<TItemType>> implements AbsListView.OnScrollListener {
    private final List<PaginatedFetcher> paginatedFetchers = new ArrayList<PaginatedFetcher>();
    private final int limit;
    private final ArrayAdapter<TItemType> adapter;
    private boolean hasMorePages = false;
    private List<TItemType> lastFetch;


    protected BaseScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        this.adapter = adapter;
        this.limit = limit;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if ((paginatedFetchers.size() == 0) && (totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
            load();
        }
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void reset() {
        lastFetch = null;
        for (PaginatedFetcher paginatedFetcher : paginatedFetchers) {
            paginatedFetcher.cancel();
        }
        adapter.clear();
        setHasMorePages(true);
    }

    public void load() {

        //Clear pending fetcherLoader
        for (PaginatedFetcher paginatedFetcher : paginatedFetchers) {
            paginatedFetcher.cancel();
        }

        //Create a new fetcherLoader
        PaginatedFetcher paginatedFetcher = new PaginatedFetcher(getOffset(), limit);
        paginatedFetchers.add(paginatedFetcher);
        onFetchStarted();
        paginatedFetcher.run();
    }

    public void stop() {
        for (PaginatedFetcher paginatedFetcher : paginatedFetchers) {
            paginatedFetcher.cancel();
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
        adapter.addAll(result);
        onFetchEnded();
        paginatedFetchers.clear();
    }

    protected void onFetchCancelled() {
        onFetchEnded();
        paginatedFetchers.clear();
    }

    protected void onFetchFailed() {
        onFetchError();
        paginatedFetchers.clear();
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

    protected void setHasMorePages(boolean value) {
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
        private ApiService.BasicApiCallback<Void> callback;

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

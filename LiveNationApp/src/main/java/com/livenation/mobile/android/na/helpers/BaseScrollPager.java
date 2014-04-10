package com.livenation.mobile.android.na.helpers;

import android.widget.AbsListView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cchilton on 3/11/14.
 */
public abstract class BaseScrollPager<TItemType> implements AbsListView.OnScrollListener {
    private final List<FetchLoader> fetchLoaders = new ArrayList<FetchLoader>();
    private final int limit;
    private final ArrayAdapter<TItemType> adapter;
    private boolean hasMorePages = false;

    protected BaseScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        this.adapter = adapter;
        this.limit = limit;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if ((fetchLoaders.size() == 0) && (totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
            load();
        }
    }

    public void reset() {
        for (FetchLoader fetchLoader : fetchLoaders) {
            fetchLoader.cancel();
        }
        adapter.clear();
        setHasMorePages(true);
    }

    public void load() {
        for (FetchLoader fetchLoader : fetchLoaders) {
            fetchLoader.cancel();
        }
        FetchLoader fetchLoader = new FetchLoader(getOffset(), limit);
        registerLoader(fetchLoader);
        onFetchStarted();
        fetchLoader.run();
    }

    protected int getOffset() {
        return adapter.getCount();
    }

    public void onNoMorePages() {
        onFetchEnded();
    }

    ;

    public List<FetchLoader> getFetchLoaders() {
        return fetchLoaders;
    }

    public void registerLoader(FetchLoader fetchLoader) {
        this.fetchLoaders.add(fetchLoader);
    }

    public void unregisterLoader(FetchLoader fetchLoader) {
        fetchLoaders.remove(fetchLoader);
    }

    public abstract FetchRequest<TItemType> getFetchRequest(int offset, int limit, FetchResultHandler callback);

    public abstract void onFetchStarted();

    public abstract void onFetchEnded();

    protected void onFetchResult(FetchLoader fetchLoader) {
        adapter.addAll(fetchLoader.getResult());
        onFetchEnded();
        unregisterLoader(fetchLoader);
    }

    protected void onLoaderCancelled(FetchLoader fetchLoader) {
        onFetchEnded();
        unregisterLoader(fetchLoader);
    }

    protected void setHasMorePages(boolean value) {
        hasMorePages = value;
    }

    public static interface FetchResultHandler<TItemType> {
        void deliverResult(List<TItemType> result);
    }

    protected class FetchLoader implements Runnable, FetchResultHandler<TItemType> {
        private final int offset;
        private final int limit;
        private final FetchRequest<TItemType> fetchRequest;
        private boolean cancelled = false;
        private List<TItemType> result;

        private FetchLoader(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
            fetchRequest = BaseScrollPager.this.getFetchRequest(offset, limit, this);
        }

        @Override
        public void run() {
            if (hasMorePages) {
                fetchRequest.run();
            } else {
                onNoMorePages();
            }
        }

        @Override
        public void deliverResult(List<TItemType> result) {
            this.setResult(result);
            if (!isCancelled()) {
                onFetchResult(FetchLoader.this);
            }
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            cancelled = true;
            getFetchRequest().cancel();
            onLoaderCancelled(this);
        }

        public FetchRequest<TItemType> getFetchRequest() {
            return fetchRequest;
        }

        public List<TItemType> getResult() {
            return result;
        }

        public void setResult(List<TItemType> result) {
            this.result = result;
        }
    }

    protected abstract class FetchRequest<TItemType> implements Runnable {
        private final int offset;
        private final int limit;
        private final FetchResultHandler<TItemType> fetchResultHandler;

        protected FetchRequest(int offset, int limit, FetchResultHandler<TItemType> fetchResultHandler) {
            this.offset = offset;
            this.limit = limit;
            this.fetchResultHandler = fetchResultHandler;
        }

        public int getOffset() {
            return offset;
        }

        public int getLimit() {
            return limit;
        }

        public FetchResultHandler<TItemType> getFetchResultHandler() {
            return fetchResultHandler;
        }

        public abstract void cancel();
    }
}

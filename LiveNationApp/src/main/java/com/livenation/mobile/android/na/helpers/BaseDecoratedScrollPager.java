package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemType extends IdEquals<TItemType>> extends BaseScrollPager<TItemType> {
    private View listLoadingView;
    private List<? extends TItemType> lastFetch;

    protected BaseDecoratedScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        super(limit, adapter);

        Context context = adapter.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        listLoadingView = inflater.inflate(R.layout.view_loading, null);
    }

    public void setupListView(StickyListHeadersListView listView) {
        listView.setOnScrollListener(this);
        listView.addFooterView(listLoadingView);
    }

    @Override
    public void reset() {
        super.reset();
        lastFetch = null;
    }

    @Override
    public void onFetchResult(FetchLoader fetchLoader) {
        List<TItemType> result = fetchLoader.getResult();
        if (result.size() == 0 || itemHasAlreadyBeenFetched(result)) {
            //end of result list, or
            //found a dupe, abort adding the data to the adapter
            setHasMorePages(false);
            onNoMorePages();
            return;
        }
        lastFetch = result;
        super.onFetchResult(fetchLoader);
    }

    @Override
    public void onFetchStarted() {
        listLoadingView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFetchEnded() {
        listLoadingView.setVisibility(View.GONE);
    }

    public abstract void stop();

    private boolean itemHasAlreadyBeenFetched(List<? extends TItemType> newFetch) {
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

}

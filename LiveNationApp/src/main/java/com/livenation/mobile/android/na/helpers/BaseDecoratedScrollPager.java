package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemType extends IdEquals<TItemType>> extends BaseScrollPager<TItemType> {
    private List<? extends TItemType> lastFetch;
    private final View listLoadingView;
    /*
    Use a frame layout to contain our loading view. This is necessary since Android doesn't like direct
    modification to the footview's view, and will leave its whitespace visible even if the view is
    set to View.GONE :-/
    http://stackoverflow.com/questions/4317778/hide-footer-view-in-listview?rq=1
    http://stackoverflow.com/questions/7576099/hiding-footer-in-listview
     */
    private final ViewGroup footerBugHack;
    private final FrameLayout.LayoutParams footerParams;

    protected BaseDecoratedScrollPager(int limit, ArrayAdapter<TItemType> adapter) {
        super(limit, adapter);

        Context context = adapter.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        listLoadingView = inflater.inflate(R.layout.view_loading, null);
        footerParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        footerBugHack = new FrameLayout(context);
    }

    public void connectListView(StickyListHeadersListView listView) {
        listView.setOnScrollListener(this);
        listView.addFooterView(footerBugHack);
    }

    @Override
    public void reset() {
        super.reset();
        lastFetch = null;
    }

    @Override
    public void onFetchResult(FetchLoader fetchLoader) {
        List<TItemType> result = fetchLoader.getResult();
        if (result.size() == 0 || hasItemAlreadyBeenFetched(result)) {
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
        footerBugHack.addView(listLoadingView);
    }

    @Override
    public void onFetchEnded() {
        footerBugHack.removeAllViews();
    }

    public abstract void stop();

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

}

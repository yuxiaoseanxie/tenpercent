package com.livenation.mobile.android.na.pagination;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemTypeOutput extends IdEquals<TItemTypeOutput>, TItemTypeInput> extends BaseScrollPager<TItemTypeOutput> {
    private final EmptyListViewControl listLoadingView;
    /*
    Use a frame layout to contain our loading view. This is necessary since Android doesn't like direct
    modification to the footview's view, and will leave its whitespace visible even if the view is
    set to View.GONE :-/
    http://stackoverflow.com/questions/4317778/hide-footer-view-in-listview?rq=1
    http://stackoverflow.com/questions/7576099/hiding-footer-in-listview
     */
    private final ViewGroup footerBugHack;
    protected EmptyListViewControl emptyView;
    private View.OnClickListener retryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setBlocked(false);
            load();
        }
    };
    protected static final int DEFAULT_LIMIT = 10;

    protected BaseDecoratedScrollPager(int limit, ArrayAdapter<TItemTypeOutput> adapter) {
        super(limit, adapter);

        Context context = adapter.getContext();
        listLoadingView = new EmptyListViewControl(context);
        listLoadingView.setRetryOnClickListener(retryClickListener);
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
        listLoadingView.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        if (null != emptyView) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        }
        footerBugHack.removeAllViews();
        footerBugHack.addView(listLoadingView);
    }

    @Override
    public void onFetchEnded() {
        footerBugHack.removeAllViews();
        if (getAdapter().getCount() == 0) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }
    }

    @Override
    public void onFetchError() {
        listLoadingView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
        setBlocked(true);
    }

    @Override
    public void fetch(final int offset, final int limit, final ApiService.BasicApiCallback<List<TItemTypeOutput>> callback) {
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                fetch(apiService, offset, limit, callback);
            }

            @Override
            public void onApiServiceNotAvailable() {
                callback.onErrorResponse(null);
            }
        });
    }

    protected abstract void fetch(LiveNationApiService apiService, final int offset, final int limit, ApiService.BasicApiCallback<List<TItemTypeOutput>> callback);

    public void setEmptyView(final EmptyListViewControl emptyView) {
        this.emptyView = emptyView;
        this.emptyView.setRetryOnClickListener(retryClickListener);
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {}

            @Override
            public void onApiServiceNotAvailable() {
                emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
            }
        });
    }
}

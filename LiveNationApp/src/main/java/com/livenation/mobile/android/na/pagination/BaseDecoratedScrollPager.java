package com.livenation.mobile.android.na.pagination;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.viewcontroller.RefreshBarController;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.RefreshBar;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.RecommendationSetsParameters;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;


/**
 * Created by cchilton on 3/12/14.
 */
public abstract class BaseDecoratedScrollPager<TItemTypeOutput extends IdEquals<TItemTypeOutput>, TItemTypeInput> extends BaseScrollPager<TItemTypeOutput> implements ApiService.BasicApiCallback<TItemTypeInput> {
    private final View listLoadingView;
    /*
    Use a frame layout to contain our loading view. This is necessary since Android doesn't like direct
    modification to the footview's view, and will leave its whitespace visible even if the view is
    set to View.GONE :-/
    http://stackoverflow.com/questions/4317778/hide-footer-view-in-listview?rq=1
    http://stackoverflow.com/questions/7576099/hiding-footer-in-listview
     */
    private final ViewGroup footerBugHack;
    protected ApiService.BasicApiCallback<List<TItemTypeOutput>> callback;
    protected EmptyListViewControl emptyView;
    private RefreshBar refreshBar;
    private RefreshBarController refreshBarController;
    private View.OnClickListener retryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            load();
        }
    };
    protected static final int DEFAULT_LIMIT = 10;

    protected BaseDecoratedScrollPager(int limit, ArrayAdapter<TItemTypeOutput> adapter) {
        super(limit, adapter);

        Context context = adapter.getContext();
        listLoadingView = new EmptyListViewControl(context);
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
        footerBugHack.addView(listLoadingView);
    }

    @Override
    public void onFetchEnded() {
        footerBugHack.removeAllViews();
    }

    @Override
    public void onFetchError() {
        footerBugHack.removeAllViews();
        if (!isFirstPage && refreshBarController != null) {
            refreshBarController.showRefreshBar(false);
        }
        //TODO find a way to notify the user an error occurred
    }

    @Override
    public void fetch(final int offset, final int limit, final ApiService.BasicApiCallback<List<TItemTypeOutput>> callback) {
        this.callback = callback;
        if (emptyView != null) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        }
        LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
            @Override
            public void onApiServiceAttached(LiveNationApiService apiService) {
                fetch(apiService, offset, limit, callback);
            }

            @Override
            public void onApiServiceNotAvailable() {
                if (emptyView != null) {
                    emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                }
            }
        });
    }

    protected abstract void fetch(LiveNationApiService apiService, final int offset, final int limit, ApiService.BasicApiCallback<List<TItemTypeOutput>> callback);

    @Override
    public void onResponse(TItemTypeInput response) {
        List<TItemTypeOutput> result = (List<TItemTypeOutput>) response;
        callback.onResponse(result);

        if (emptyView != null) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }
    }

    @Override
    public void onErrorResponse(LiveNationError error) {
        callback.onErrorResponse(error);
        if (emptyView != null) {
            emptyView.setViewMode(EmptyListViewControl.ViewMode.RETRY);
        }
    }

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

    public void setRefreshBarView(RefreshBar refreshBar) {
        this.refreshBar = refreshBar;
        refreshBarController = new RefreshBarController(refreshBar, retryClickListener);
    }
}

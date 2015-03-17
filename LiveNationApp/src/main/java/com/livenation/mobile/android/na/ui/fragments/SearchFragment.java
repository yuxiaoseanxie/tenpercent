package com.livenation.mobile.android.na.ui.fragments;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.helpers.SearchForText;
import com.livenation.mobile.android.na.ui.adapters.SearchAdapter;
import android.mobile.livenation.com.livenationui.fragment.base.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.platform.api.service.livenation.impl.BasicApiCallback;
import com.livenation.mobile.android.platform.api.transport.error.LiveNationError;

import java.util.List;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by elodieferrais on 11/6/14.
 */
public abstract class SearchFragment<ResultType> extends LiveNationFragment implements SearchForText, BasicApiCallback<List<ResultType>>, ListView.OnItemClickListener {

    protected SearchAdapter<ResultType> adapter;
    private EmptyListViewControl emptyListViewControl;
    private int pendingRequestCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        adapter = getAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ListView listView = (ListView) view.findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setOnItemClickListener(this);

        emptyListViewControl = (EmptyListViewControl) view.findViewById(R.id.fragment_search_empty_list_control);
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);
        return view;
    }

    @Override
    public void searchForText(String text) {
        if (TextUtils.isEmpty(text)) {
            adapter.clear();
            emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);
            return;
        }
        synchronized (this) {
            pendingRequestCount++;
        }
        if (adapter.getCount() == 0) {
            emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.LOADING);
        } else {
            adapter.startHighlightedMode();
        }
        searchFor(text, this);
    }

    @Override
    public void onResponse(List<ResultType> response) {
        adapter.clear();
        pendingRequestCount--;
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.INACTIVE);
        if (pendingRequestCount == 0) {
            adapter.stopHighlightedMode();
        }
        if (response.size() == 0) {
            emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
        }
        for (ResultType result : response) {
            adapter.add(result);
        }

    }

    @Override
    public void onErrorResponse(LiveNationError error) {
    }

    abstract public SearchAdapter<ResultType> getAdapter();

    abstract public void searchFor(String text, BasicApiCallback<List<ResultType>> callback);
}
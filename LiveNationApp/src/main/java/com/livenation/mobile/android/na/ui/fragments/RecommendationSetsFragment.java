/*
 * 
 * @author Charlie Chilton 2014/01/24
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.Constants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.helpers.TaggedReference;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.EmptyListViewControl;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.ApiService;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet;
import com.livenation.mobile.android.platform.api.service.livenation.impl.parameter.RecommendationSetsParameters;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class RecommendationSetsFragment extends LiveNationFragment implements OnItemClickListener, ApiServiceBinder {
    private StickyListHeadersListView listView;
    private EventAdapter adapter;
    private ScrollPager scrollPager;
    private EmptyListViewControl emptyListViewControl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new EventAdapter(getActivity(), new ArrayList<TaggedEvent>());
        scrollPager = new ScrollPager(adapter);
        setRetainInstance(true);
        LiveNationApplication.get().getApiHelper().persistentBindApi(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_shows_list, container, false);
        listView = (StickyListHeadersListView) view.findViewById(id.fragment_all_shows_list);
        listView.setOnItemClickListener(RecommendationSetsFragment.this);
        listView.setAdapter(adapter);
        emptyListViewControl = (EmptyListViewControl) view.findViewById(android.R.id.empty);
        listView.setEmptyView(emptyListViewControl);
        listView.setDivider(null);
        listView.setAreHeadersSticky(false);
        scrollPager.connectListView(listView);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        scrollPager.stop();
        LiveNationApplication.get().getApiHelper().persistentUnbindApi(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Parcelable listState = listView.getWrappedList().onSaveInstanceState();
        outState.putParcelable(getViewKey(listView), listState);
    }

    @Override
    public void applyInstanceState(Bundle state) {
        Parcelable listState = state.getParcelable(getViewKey(listView));
        if (null != listState) {
            listView.getWrappedList().onRestoreInstanceState(listState);
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Intent intent = new Intent(getActivity(), ShowActivity.class);
        Event event = adapter.getItem(position).get();

        Bundle args = SingleEventPresenter.getAruguments(event.getId());
        SingleEventPresenter.embedResult(args, event);
        intent.putExtras(args);

        startActivity(intent);
    }

    @Override
    public void onApiServiceAttached(LiveNationApiService apiService) {
        scrollPager.reset();
        scrollPager.load();
    }

    @Override
    public void onApiServiceNotAvailable() {
        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
    }

    private static class TaggedEvent extends TaggedReference<Event, Boolean> implements IdEquals<TaggedEvent> {

        private TaggedEvent(Event event) {
            super(event);
        }

        public boolean isPersonal() {
            return getTag();
        }

        @Override
        public boolean idEquals(TaggedEvent target) {
            return get().idEquals(target.get());
        }
    }

    public class EventAdapter extends ArrayAdapter<TaggedEvent> implements StickyListHeadersAdapter {
        private LayoutInflater inflater;

        public EventAdapter(Context context, List<TaggedEvent> items) {
            super(context, android.R.layout.simple_list_item_1, items);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;

            if (null == convertView) {
                view = inflater.inflate(R.layout.list_show_item_v2, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            Event event = getItem(position).get();
            holder.getTitle().setText(event.getName());
            holder.getLocation().setText(event.getVenue().getName());

            if (event.getLineup().size() > 0) {
                String imageKey = event.getLineup().get(0).getBestImageKey(new String[]{"tap", "mobile_detail"});
                holder.getImage().setImageUrl(event.getLineup().get(0).getImageURL(imageKey), getImageLoader());
            } else {
                holder.getImage().setImageUrl(null, getImageLoader());
            }

            holder.getDate().setDate(event.getLocalStartTime());

            return view;
        }

        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            View view = null;
            ViewHeaderHolder holder = null;
            if (null == convertView) {
                view = inflater.inflate(R.layout.list_recommended_header, null);
                holder = new ViewHeaderHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHeaderHolder) view.getTag();
            }

            TextView text = holder.getText();

            if (getItem(position).isPersonal()) {
                text.setText(getString(R.string.recommendations_title_personal));
            } else {
                text.setText(getString(R.string.recommendations_title_popular));
            }
            return view;
        }

        @Override
        public long getHeaderId(int position) {
            if (getItem(position).getTag()) {
                return 1;
            }
            return 0;
        }

        private class ViewHolder {
            private final TextView title;
            private final TextView location;
            private final VerticalDate date;
            private final NetworkImageView image;

            public ViewHolder(View view) {
                this.title = (TextView) view.findViewById(id.list_generic_show_title);
                this.location = (TextView) view.findViewById(id.list_generic_show_location);
                this.date = (VerticalDate) view.findViewById(id.list_generic_show_date);
                this.image = (NetworkImageView) view.findViewById(id.list_item_show_image);
            }

            public TextView getTitle() {
                return title;
            }

            public TextView getLocation() {
                return location;
            }

            public VerticalDate getDate() {
                return date;
            }

            public NetworkImageView getImage() {
                return image;
            }
        }

        private class ViewHeaderHolder {
            private final TextView text;

            public ViewHeaderHolder(View view) {
                this.text = (TextView) view.findViewById(id.list_recommended_header_textview);
            }

            public TextView getText() {
                return text;
            }
        }
    }

    private class ScrollPager extends BaseDecoratedScrollPager<TaggedEvent> {

        private ScrollPager(ArrayAdapter<TaggedEvent> adapter) {
            super(10, adapter);
        }

        @Override
        public FetchRequest<TaggedEvent> getFetchRequest(int offset, int limit, FetchResultHandler callback) {
            FetchRequest request = new EventsFetchRequest(offset, limit, callback);
            return request;
        }

        @Override
        public void stop() {
            for (FetchLoader fetchLoader : getFetchLoaders()) {
                fetchLoader.cancel();
            }
        }

        private class EventsFetchRequest extends FetchRequest<TaggedEvent> implements ApiService.BasicApiCallback<List<RecommendationSet>> {

            private EventsFetchRequest(int offset, int limit, FetchResultHandler<TaggedEvent> fetchResultHandler) {
                super(offset, limit, fetchResultHandler);
            }

            @Override
            public void run() {
                LiveNationApplication.get().getApiHelper().bindApi(new ApiServiceBinder() {
                    @Override
                    public void onApiServiceAttached(LiveNationApiService apiService) {
                        RecommendationSetsParameters params = new RecommendationSetsParameters();
                        params.setPage(getOffset(), getLimit());
                        params.setLocation(apiService.getApiConfig().getLat(), apiService.getApiConfig().getLng());
                        params.setIncludes(new String[]{"personal", "popular"});
                        params.setRadius(Constants.DEFAULT_RADIUS);
                        apiService.getRecommendationSets(params, EventsFetchRequest.this);
                    }

                    @Override
                    public void onApiServiceNotAvailable() {
                        emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
                    }
                });
            }

            @Override
            public void cancel() {
                //TODO: cancel any inprogress API request here
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("fail", "fail");
                emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.RETRY);
            }

            @Override
            public void onResponse(List<RecommendationSet> response) {
                List<TaggedEvent> result = new ArrayList<TaggedEvent>();

                for (RecommendationSet set : response) {
                    boolean isPersonal = false;
                    if ("personal".equalsIgnoreCase(set.getName())) {
                        isPersonal = true;
                    }
                    for (Event event : set.getEvents()) {
                        TaggedEvent taggedEvent = new TaggedEvent(event);
                        taggedEvent.setTag(isPersonal);
                        result.add(taggedEvent);
                    }
                }

                getFetchResultHandler().deliverResult(result);

                if (result.size() == 0) {
                    emptyListViewControl.setViewMode(EmptyListViewControl.ViewMode.NO_DATA);
                }
            }
        }
    }
}

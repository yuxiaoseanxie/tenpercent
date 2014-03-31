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
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.R.id;
import com.livenation.mobile.android.na.app.ApiServiceBinder;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.BaseDecoratedScrollPager;
import com.livenation.mobile.android.na.presenters.SingleEventPresenter;
import com.livenation.mobile.android.na.presenters.views.EventsView;
import com.livenation.mobile.android.na.ui.ShowActivity;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.LiveNationApiService;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class RecommendationsFragment extends LiveNationFragment implements OnItemClickListener, ApiServiceBinder {
	private StickyListHeadersListView listView;
	private EventAdapter adapter;
    private ScrollPager scrollPager;

	private static SimpleDateFormat sdf = new SimpleDateFormat(LiveNationApiService.DATE_TIME_Z_FORMAT, Locale.US);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		adapter = new EventAdapter(getActivity(), new ArrayList<Event>());
        scrollPager = new ScrollPager(adapter);

        LiveNationApplication.get().getApiHelper().persistentBindApi(this);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_shows_list, container, false);
		listView = (StickyListHeadersListView) view.findViewById(id.fragment_all_shows_list);
		listView.setOnItemClickListener(RecommendationsFragment.this);
		listView.setAdapter(adapter);
		listView.setEmptyView(view.findViewById(android.R.id.empty));
        scrollPager.connectListView(listView);

		return view;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveNationApplication.get().getApiHelper().persistentUnbindApi(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        scrollPager.stop();
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
		Event event = adapter.getItem(position);

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

    public class EventAdapter extends ArrayAdapter<Event> implements StickyListHeadersAdapter {
        private LayoutInflater inflater;

        public EventAdapter(Context context, List<Event> items) {
            super(context, android.R.layout.simple_list_item_1, items);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            View view = null;

            if (null == convertView) {
                view = inflater.inflate(R.layout.list_show_item, null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder) convertView.getTag();
            }

            Event event = getItem(position);
            holder.getTitle().setText(event.getName());
            holder.getLocation().setText(event.getVenue().getName());

            //TODO: Move date parsing to Data Model Entity helper. This is ugly
            try {
                Date date = sdf.parse(event.getStartTime());
                holder.getDate().setDate(date);
            } catch (ParseException e) {
                //should never happen, burn everything
                throw new RuntimeException(e);
            }

            return view;
        }

        @Override
        public View getHeaderView(int position, View convertView,
                                  ViewGroup parent) {
            View view = null;
            ViewHeaderHolder holder = null;
            if (null == convertView) {
                view = inflater.inflate(R.layout.list_show_header, null);
                holder = new ViewHeaderHolder(view);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHeaderHolder) view.getTag();
            }

            TextView text = holder.getText();

            //TODO: refactor this into Model helpers (inline or sub-helper classes?)
            String dateRaw = getItem(position).getStartTime();
            try {
                Date date = sdf.parse(dateRaw);
                String dateValue = DateFormat.format("MMMM", date).toString();
                text.setText(dateValue);
            } catch (ParseException e) {
                throw new IllegalStateException("Unparsable date: " + dateRaw);
            }

            return view;
        }

        @Override
        public long getHeaderId(int position) {
            String dateRaw = getItem(position).getStartTime();
            try {

                Date date = sdf.parse(dateRaw);
                String dateValue = DateFormat.format("yyyyMM", date).toString();
                return Long.valueOf(dateValue);
            } catch (ParseException e) {
                throw new IllegalStateException("Unparsable date: " + dateRaw);
            }
        }

        private class ViewHolder {
            private final TextView title;
            private final TextView location;
            private final VerticalDate date;

            public ViewHolder(View view) {
                this.title = (TextView) view.findViewById(R.id.list_generic_show_title);
                this.location = (TextView) view.findViewById(R.id.list_generic_show_location);
                this.date = (VerticalDate) view.findViewById(R.id.list_generic_show_date);
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
        }

        private class ViewHeaderHolder {
            private final TextView text;

            public ViewHeaderHolder(View view) {
                this.text = (TextView) view.findViewById(R.id.list_show_header_textview);
            }

            public TextView getText() {
                return text;
            }
        }
    }

    private class ScrollPager extends BaseDecoratedScrollPager<Event> {

        private ScrollPager(ArrayAdapter<Event> adapter) {
            super(10, adapter);
        }

        @Override
        public FetchRequest<Event> getFetchRequest(int offset, int limit, FetchResultHandler callback) {
            FetchRequest request = new EventsFetchRequest(offset, limit, callback);
            return request;
        }

        @Override
        public void stop() {
            for (FetchLoader fetchLoader : getFetchLoaders()) {
                fetchLoader.cancel();
            }
        }

        private class EventsFetchRequest extends FetchRequest<Event> implements EventsView {

            private EventsFetchRequest(int offset, int limit, FetchResultHandler<Event> fetchResultHandler) {
                super(offset, limit, fetchResultHandler);
            }

            @Override
            public void run() {
                Bundle args = getRecommendationsPresenter().getArgs(getOffset(), getLimit());
                getRecommendationsPresenter().initialize(getActivity(), args, this);
            }

            @Override
            public void setEvents(List<Event> events) {
                getFetchResultHandler().deliverResult(events);
            }

            @Override
            public void cancel() {
                getEventsPresenter().cancel(this);
            }
        }
    }
}

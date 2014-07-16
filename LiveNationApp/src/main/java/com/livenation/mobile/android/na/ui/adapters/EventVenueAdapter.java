package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.ui.views.FavoriteCheckBox;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Favorite;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Venue;
import com.livenation.mobile.android.platform.init.callback.ConfigCallback;
import com.livenation.mobile.android.platform.init.provider.ProviderManager;
import com.livenation.mobile.android.platform.init.proxy.LiveNationConfig;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by elodieferrais on 4/22/14.
 */
public class EventVenueAdapter extends ArrayAdapter<Event> implements StickyListHeadersAdapter, ConfigCallback {
    private static final String START_TIME_FORMAT = "h:mm a zzz";
    private static final SimpleDateFormat START_TIME_FORMATTER = new SimpleDateFormat("h:mm a zzz");
    private static float METERS_IN_A_MILE = 1609.34f;
    private LayoutInflater inflater;
    private Double lat;
    private Double lng;

    public EventVenueAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<Event>());
        inflater = LayoutInflater.from(context);
        ProviderManager providerManager = new ProviderManager();
        providerManager.getConfigReadyFor(this, ProviderManager.ProviderType.LOCATION);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        View view = null;

        if (null == convertView) {
            view = inflater.inflate(R.layout.list_show_nearby_item, null);
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);
        holder.getTitle().setText(event.getDisplayName());

        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        START_TIME_FORMATTER.setTimeZone(timeZone);
        String startTime = START_TIME_FORMATTER.format(event.getLocalStartTime());

        holder.getStartTime().setText(startTime);
        holder.getDate().setDate(event.getLocalStartTime(), timeZone);

        return view;
    }

    @Override
    public View getHeaderView(int position, View convertView,
                              ViewGroup parent) {
        View view = null;
        ViewHeaderHolder holder = null;
        if (null == convertView) {
            view = inflater.inflate(R.layout.list_venue_nearby_header, null);
            holder = new ViewHeaderHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHeaderHolder) view.getTag();
        }

        TextView title = holder.getVenueTitle();
        Event event = getItem(position);
        title.setText(event.getVenue().getName());

        TextView location = holder.getLocation();
        location.setText(event.getVenue().getAddress().getSmallFriendlyAddress(false));
        TextView distance = holder.getDistance();

        if (null != lat && null != lng) {
            distance.setVisibility(View.VISIBLE);
            float[] result = new float[1];
            double venueLat = Double.valueOf(event.getVenue().getLat());
            double venueLng = Double.valueOf(event.getVenue().getLng());
            Location.distanceBetween(lat, lng, venueLat, venueLng, result);
            float miles = result[0] / METERS_IN_A_MILE;
            distance.setText(String.format("%.1f mi", miles));
        } else {
            distance.setVisibility(View.GONE);
        }
        holder.getFavorite().setChecked(false);

        Venue venue = event.getVenue();
        holder.getFavorite().bindToFavorite(Favorite.FAVORITE_VENUE, venue.getName(), venue.getNumericId(), LiveNationApplication.get().getFavoritesPresenter(), AnalyticsCategory.NEARBY);

        return view;
    }

    @Override
    public long getHeaderId(int position) {
        Event event = getItem(position);
        long venueId = event.getVenue().getNumericId();
        return venueId;
    }

    @Override
    public void onResponse(LiveNationConfig response) {
        this.lat = response.getLat();
        this.lng = response.getLng();
        notifyDataSetChanged();
    }

    @Override
    public void onErrorResponse(int errorCode) {
    }

    private class ViewHolder {
        private final TextView title;
        private final TextView startTime;
        private final VerticalDate date;

        public ViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.list_nearby_show_title);
            this.startTime = (TextView) view.findViewById(R.id.list_nearby_show_time);
            this.date = (VerticalDate) view.findViewById(R.id.list_nearby_show_date);
        }

        public TextView getTitle() {
            return title;
        }

        public TextView getStartTime() {
            return startTime;
        }

        public VerticalDate getDate() {
            return date;
        }
    }

    private class ViewHeaderHolder {
        private final TextView venueTitle;
        private final TextView venueLocation;
        private final FavoriteCheckBox venueFavorite;
        private final TextView venueDistance;
        private final ViewGroup venueTextContainer;

        public ViewHeaderHolder(View view) {
            this.venueTitle = (TextView) view.findViewById(R.id.list_venue_header_title);
            this.venueLocation = (TextView) view.findViewById(R.id.list_venue_header_location);
            this.venueFavorite = (FavoriteCheckBox) view.findViewById(R.id.list_venue_header_checkbox);
            this.venueDistance = (TextView) view.findViewById(R.id.list_venue_header_distance);
            this.venueTextContainer = (ViewGroup) view.findViewById(R.id.list_venue_header_text_container);
        }

        public TextView getVenueTitle() {
            return venueTitle;
        }

        public TextView getLocation() {
            return venueLocation;
        }

        public FavoriteCheckBox getFavorite() {
            return venueFavorite;
        }

        public TextView getDistance() {
            return venueDistance;
        }
    }
}
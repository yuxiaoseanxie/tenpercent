package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.helpers.TaggedReference;
import com.livenation.mobile.android.na.ui.SearchActivity;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by elodieferrais on 4/22/14.
 */
public class RecommendationsAdapter extends ArrayAdapter<RecommendationsAdapter.RecommendationItem> implements StickyListHeadersAdapter {
    private LayoutInflater inflater;
    private static final int ITEM_TYPE_EVENT = 0;
    private static final int ITEM_TYPE_UPSELL_SMALL = 1;
    private static final int ITEM_TYPE_UPSELL_MEDIUM = 2;
    private static final int ITEM_TYPE_UPSELL_LARGE = 3;
    private static final int ITEM_TYPE_COUNT = 4;

    public RecommendationsAdapter(Context context, List<RecommendationItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == ITEM_TYPE_EVENT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int itemType = getItemViewType(position);
        switch (itemType) {
            case ITEM_TYPE_UPSELL_SMALL:
                return getRecommendationsUpsellSmall(inflater, parent);
            case ITEM_TYPE_UPSELL_MEDIUM:
                return getRecommendationsUpsellMedium(inflater, parent);
            case ITEM_TYPE_UPSELL_LARGE:
                return getRecommendationsUpsellLarge(inflater, parent);
        }

        View view = null;
        EventViewHolder holder;
        if (null == convertView) {
            view = inflater.inflate(R.layout.list_show_item_v2, null);
            holder = new EventViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (EventViewHolder) convertView.getTag();
        }

        Event event = getItem(position).get();

        holder.getTitle().setText(event.getName());
        holder.getLocation().setText(event.getVenue().getName());

        if (event.getLineup().size() > 0) {
            String imageKey = event.getLineup().get(0).getBestImageKey(new String[]{"tap", "mobile_detail"});
            holder.getImage().setImageUrl(event.getLineup().get(0).getImageURL(imageKey), LiveNationApplication.get().getImageLoader());
        } else {
            holder.getImage().setImageUrl(null, LiveNationApplication.get().getImageLoader());
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
        switch (getItem(position).getTag()) {
            case RecommendationItem.EVENT_POPULAR:
                text.setText(getContext().getString(R.string.recommendations_title_popular));
                break;
            default:
                text.setText(getContext().getString(R.string.recommendations_title_personal));
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        //simply returning getItem(position).getTag() here would prevent efficient view recycling in getView(),
        //as views for personal and popular would be in separate view recycle pools. 
        int itemType = getItem(position).getTag();
        switch (itemType) {
            case RecommendationItem.FAVORITE_UPSELL_LARGE:
                return ITEM_TYPE_UPSELL_LARGE;
            case RecommendationItem.FAVORITE_UPSELL_MEDIUM:
                return ITEM_TYPE_UPSELL_MEDIUM;
            case RecommendationItem.FAVORITE_UPSELL_SMALL:
                return ITEM_TYPE_UPSELL_SMALL;
            default:
                return ITEM_TYPE_EVENT;
        }
    }

    @Override
    public int getViewTypeCount() {
        return ITEM_TYPE_COUNT;
    }

    @Override
    public long getHeaderId(int position) {
        int itemType = getItem(position).getTag();
        switch (itemType) {
            case RecommendationItem.EVENT_PERSONAL:
                return RecommendationItem.EVENT_PERSONAL;
            case RecommendationItem.EVENT_POPULAR:
                return RecommendationItem.EVENT_POPULAR;
            default:
                //group all recommendation upsells to the personal header group
                return RecommendationItem.EVENT_PERSONAL;
        }
     }

    private View getRecommendationsUpsellSmall(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_recommendation_upsell_small, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra(SearchActivity.SEARCH_MODE, SearchActivity.SEARCH_MODE_ARTIST_ONLY);
                getContext().startActivity(intent);
            }
        });
        return view;
    }

    private View getRecommendationsUpsellMedium(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_recommendation_upsell_medium, parent, false);
        view.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra(SearchActivity.SEARCH_MODE, SearchActivity.SEARCH_MODE_ARTIST_ONLY);
                getContext().startActivity(intent);
            }
        });
        return view;
    }

    private View getRecommendationsUpsellLarge(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_recommendation_upsell_large, parent, false);
        view.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveNationAnalytics.track(AnalyticConstants.FACEBOOK_CONNECT_TAP);
                Intent intent = new Intent(getContext(), SsoActivity.class);
                intent.putExtra(SsoActivity.ARG_PROVIDER_ID, SsoManager.SSO_TYPE.SSO_FACEBOOK.name());
                getContext().startActivity(intent);
            }
        });
        view.findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra(SearchActivity.SEARCH_MODE, SearchActivity.SEARCH_MODE_ARTIST_ONLY);
                getContext().startActivity(intent);
            }
        });
        return view;
    }

    private class EventViewHolder {
        private final TextView title;
        private final TextView location;
        private final VerticalDate date;
        private final NetworkImageView image;

        public EventViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.list_generic_show_title);
            this.location = (TextView) view.findViewById(R.id.list_generic_show_location);
            this.date = (VerticalDate) view.findViewById(R.id.list_generic_show_date);
            this.image = (NetworkImageView) view.findViewById(R.id.list_item_show_image);
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
            this.text = (TextView) view.findViewById(R.id.list_recommended_header_textview);
        }

        public TextView getText() {
            return text;
        }
    }

    public static class RecommendationItem extends TaggedReference<Event, Integer> implements IdEquals<RecommendationItem> {
        public static final int EVENT_PERSONAL = 0;
        public static final int EVENT_POPULAR = 1;
        public static final int FAVORITE_UPSELL_SMALL = 2;
        public static final int FAVORITE_UPSELL_MEDIUM = 3;
        public static final int FAVORITE_UPSELL_LARGE = 4;

        public RecommendationItem() {
            super(null);
        }

        public RecommendationItem(Event event) {
            super(event);
        }

        @Override
        public boolean idEquals(RecommendationItem target) {
            if (hasEvent()) {
                return get().idEquals(target.get()) && getTag().equals(target.getTag());
            }
            return false;
        }

        public boolean hasEvent() {
            return get() != null;
        }
    }
}

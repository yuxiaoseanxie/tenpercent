package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.analytics.AnalyticsCategory;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.DefaultImageHelper;
import com.livenation.mobile.android.na.helpers.SsoManager;
import com.livenation.mobile.android.na.helpers.TaggedReference;
import com.livenation.mobile.android.na.ui.SearchActivity;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.na.ui.views.TransitioningImageView;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;
import java.util.TimeZone;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by elodieferrais on 4/22/14.
 */
public class RecommendationsAdapter extends ArrayAdapter<RecommendationsAdapter.RecommendationItem> implements StickyListHeadersAdapter {
    private static final int ITEM_TYPE_EVENT = 0;
    private static final int ITEM_TYPE_UPSELL_DISCREET = 1;
    private static final int ITEM_TYPE_UPSELL_SEARCH = 2;
    private static final int ITEM_TYPE_UPSELL_SEARCH_WITH_FACEBOOK = 3;
    private static final int ITEM_TYPE_COUNT = 4;
    private LayoutInflater inflater;

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
            case ITEM_TYPE_UPSELL_DISCREET:
                return getRecommendationsUpsellDiscreet(inflater, parent);
            case ITEM_TYPE_UPSELL_SEARCH:
                return getRecommendationsUpsellSearch(inflater, parent);
            case ITEM_TYPE_UPSELL_SEARCH_WITH_FACEBOOK:
                return getRecommendationsUpsellSearchWithFacebook(inflater, parent);
        }

        View view;
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

        holder.getTitle().setText(event.getDisplayName());
        holder.getLocation().setText(event.getVenue().getName());

        int drawableId = DefaultImageHelper.computeDefaultTapDrawableId(getContext(), event.getNumericId());
        String imageUrl = null;

        if (event.getLineup().size() > 0) {
            String imageKey = event.getLineup().get(0).getBestImageKey(new String[]{"tap"});
            imageUrl = event.getLineup().get(0).getImageURL(imageKey);
        }

        holder.getImage().setDefaultImage(drawableId);
        holder.getImage().setImageUrl(imageUrl, LiveNationApplication.get().getImageLoader(), TransitioningImageView.LoadAnimation.FADE);

        TimeZone timeZone;
        if (event.getVenue().getTimeZone() != null) {
            timeZone = TimeZone.getTimeZone(event.getVenue().getTimeZone());
        } else {
            timeZone = TimeZone.getDefault();
        }
        holder.getDate().setDate(event.getLocalStartTime(), timeZone);

        if ((position + 1) < getCount() && getHeaderId(position) != getHeaderId(position + 1)) {
            holder.getDivider().setVisibility(View.GONE);
        } else {
            holder.getDivider().setVisibility(View.VISIBLE);
        }

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
        ImageView swoocher = holder.getSwoocher();
        switch (getItem(position).getTag()) {
            case EVENT_POPULAR:
                text.setText(getContext().getString(R.string.recommendations_title_popular));
                swoocher.setVisibility(View.VISIBLE);
                break;
            default:
                text.setText(getContext().getString(R.string.recommendations_title_personal));
                swoocher.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        //simply returning getItem(position).getTag() here would prevent efficient view recycling in getView(),
        //as views for personal and popular would be in separate view recycle pools.

        switch (getItem(position).getTag()) {
            case FAVORITE_UPSELL_SEARCH_WITH_FACEBOOK:
                return ITEM_TYPE_UPSELL_SEARCH_WITH_FACEBOOK;
            case FAVORITE_UPSELL_SEARCH:
                return ITEM_TYPE_UPSELL_SEARCH;
            case FAVORITE_UPSELL_DISCREET:
                return ITEM_TYPE_UPSELL_DISCREET;
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
        switch (getItem(position).getTag()) {
            case EVENT_PERSONAL:
                return RecommendationItem.RecommendationType.EVENT_PERSONAL.ordinal();
            case EVENT_POPULAR:
                return RecommendationItem.RecommendationType.EVENT_POPULAR.ordinal();
            default:
                //group all recommendation upsells to the personal header group
                return RecommendationItem.RecommendationType.EVENT_PERSONAL.ordinal();
        }
    }

    private View getRecommendationsUpsellDiscreet(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_recommendation_upsell_discreet, parent, false);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                launchArtistSearch();
            }
        });
        return view;
    }

    private View getRecommendationsUpsellSearch(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_recommendation_upsell_search, parent, false);
        view.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchArtistSearch();
            }
        });
        return view;
    }

    private View getRecommendationsUpsellSearchWithFacebook(LayoutInflater inflater, ViewGroup parent) {
        View view = inflater.inflate(R.layout.list_recommendation_upsell_search_with_facebook, parent, false);
        view.findViewById(android.R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LiveNationAnalytics.track(AnalyticConstants.FACEBOOK_CONNECT_TAP, AnalyticsCategory.RECOMMENDATIONS);
                Intent intent = new Intent(getContext(), SsoActivity.class);
                intent.putExtra(SsoActivity.ARG_PROVIDER_ID, SsoManager.SSO_TYPE.SSO_FACEBOOK.name());
                getContext().startActivity(intent);
            }
        });
        view.findViewById(android.R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchArtistSearch();
            }
        });
        return view;
    }

    private void launchArtistSearch() {
        LiveNationAnalytics.track(AnalyticConstants.FAVORITES_UPSELL_TAP, AnalyticsCategory.RECOMMENDATIONS);
        Intent intent = new Intent(getContext(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_MODE_KEY, SearchActivity.EXTRA_SEARCH_MODE_ARTIST_VALUE);
        getContext().startActivity(intent);
    }

    public static class RecommendationItem extends TaggedReference<Event, RecommendationItem.RecommendationType> implements IdEquals<RecommendationItem> {
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

        public static enum RecommendationType {EVENT_PERSONAL, EVENT_POPULAR, FAVORITE_UPSELL_DISCREET, FAVORITE_UPSELL_SEARCH, FAVORITE_UPSELL_SEARCH_WITH_FACEBOOK}
    }

    private class EventViewHolder {
        private final TextView title;
        private final TextView location;
        private final VerticalDate date;
        private final TransitioningImageView image;
        private final View divider;

        public EventViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.list_generic_show_title);
            this.location = (TextView) view.findViewById(R.id.list_generic_show_location);
            this.date = (VerticalDate) view.findViewById(R.id.list_generic_show_date);
            this.image = (TransitioningImageView) view.findViewById(R.id.list_item_show_image);
            this.divider = view.findViewById(R.id.list_item_show_divider);
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

        public TransitioningImageView getImage() {
            return image;
        }

        public View getDivider() {
            return divider;
        }
    }

    private class ViewHeaderHolder {
        private final TextView text;
        private final ImageView swoocher;

        public ViewHeaderHolder(View view) {
            this.text = (TextView) view.findViewById(R.id.list_recommended_header_textview);
            this.swoocher = (ImageView) view.findViewById(R.id.list_recommended_header_swoocher);
        }

        public TextView getText() {
            return text;
        }

        public ImageView getSwoocher() {
            return swoocher;
        }
    }
}

package com.livenation.mobile.android.na.ui.adapters;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.analytics.AnalyticConstants;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import android.mobile.livenation.com.livenationui.view.tools.DefaultImageHelper;
import com.livenation.mobile.android.na.ui.FavoriteSearchActivity;
import com.livenation.mobile.android.na.ui.SsoActivity;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;
import com.livenation.mobile.android.platform.sso.SsoManager;

import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.content.Intent;
import android.mobile.livenation.com.livenationui.analytics.AnalyticsCategory;
import android.mobile.livenation.com.livenationui.analytics.LiveNationAnalytics;
import android.mobile.livenation.com.livenationui.view.LayoutedTextView;
import android.mobile.livenation.com.livenationui.view.TransitioningImageView;
import android.mobile.livenation.com.livenationui.view.VerticalDate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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
            view = inflater.inflate(R.layout.list_show_item_v2, parent, false);
            holder = new EventViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (EventViewHolder) convertView.getTag();
        }

        Event event = getItem(position).event;

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
        holder.getDate().setDate(event.getLocalStartTime(), timeZone, event.getIsMegaticket());

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
            view = inflater.inflate(R.layout.list_recommended_header, parent, false);
            holder = new ViewHeaderHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHeaderHolder) view.getTag();
        }

        TextView text = holder.getText();
        switch (getItem(position).type) {
            case EVENT_POPULAR:
                text.setText(getContext().getString(R.string.recommendations_title_popular));
                text.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.swoosh_divider, 0, 0);
                text.setCompoundDrawablePadding(getContext().getResources().getDimensionPixelSize(R.dimen.gap_medium));
                text.setPadding(text.getPaddingLeft(), 0, text.getPaddingRight(), text.getPaddingBottom());
                break;
            default:
                text.setText(getContext().getString(R.string.recommendations_title_personal));
                text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                text.setCompoundDrawablePadding(0);
                text.setPadding(text.getPaddingLeft(), getContext().getResources().getDimensionPixelSize(R.dimen.gap_medium), text.getPaddingRight(), text.getPaddingBottom());
        }

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        //simply returning getItem(position).getTag() here would prevent efficient view recycling in getView(),
        //as views for personal and popular would be in separate view recycle pools.

        switch (getItem(position).type) {
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
        switch (getItem(position).type) {
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
        Intent intent = new Intent(getContext(), FavoriteSearchActivity.class);
        intent.putExtra(FavoriteSearchActivity.EXTRA_KEY_SEARCH_MODE, FavoriteSearchActivity.EXTRA_VALUE_SEARCH_MODE_ARTIST);
        intent.putExtra(FavoriteSearchActivity.EXTRA_KEY_ON_CLICK_ACTION, FavoriteSearchActivity.EXTRA_VALUE_ON_CLICK_ACTION_FAVORITE);
        getContext().startActivity(intent);
    }

    public static class RecommendationItem implements IdEquals<RecommendationItem> {
        public Event event;
        RecommendationType type;

        public RecommendationItem() {
        }

        public RecommendationItem(Event event) {
            this.event = event;

        }

        public void setTag(RecommendationType type) {
            this.type = type;
        }

        @Override
        public boolean idEquals(RecommendationItem target) {
            if (hasEvent()) {
                return event.idEquals(target.event) && type.equals(target.type);
            }
            return false;
        }

        public boolean hasEvent() {
            return event != null;
        }

        public static enum RecommendationType {EVENT_PERSONAL, EVENT_POPULAR, FAVORITE_UPSELL_DISCREET, FAVORITE_UPSELL_SEARCH, FAVORITE_UPSELL_SEARCH_WITH_FACEBOOK}
    }

    private class EventViewHolder {
        private final LayoutedTextView title;
        private final TextView location;
        private final VerticalDate date;
        private final TransitioningImageView image;
        private final View divider;

        public EventViewHolder(View view) {
            this.title = (LayoutedTextView) view.findViewById(R.id.list_generic_show_title);
            this.location = (TextView) view.findViewById(R.id.list_generic_show_location);
            this.date = (VerticalDate) view.findViewById(R.id.list_generic_show_date);
            this.image = (TransitioningImageView) view.findViewById(R.id.list_item_show_image);
            this.divider = view.findViewById(R.id.list_item_show_divider);

            title.setOnLayoutListener(new LayoutedTextView.OnLayoutListener() {
                @Override
                public void onLayouted(TextView view) {
                    int lineCount = view.getLineCount();
                    if (lineCount > 2) {
                        location.setMaxLines(1);
                        location.invalidate();
                    }
                }
            });
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

        public ViewHeaderHolder(View view) {
            this.text = (TextView) view.findViewById(R.id.list_recommended_header_textview);
        }

        public TextView getText() {
            return text;
        }
    }
}

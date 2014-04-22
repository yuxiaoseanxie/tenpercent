package com.livenation.mobile.android.na.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.helpers.TaggedReference;
import com.livenation.mobile.android.na.ui.views.VerticalDate;
import com.livenation.mobile.android.platform.api.service.livenation.helpers.IdEquals;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Event;

import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

/**
 * Created by elodieferrais on 4/22/14.
 */
public class RecommendationsAdapter extends ArrayAdapter<RecommendationsAdapter.TaggedEvent> implements StickyListHeadersAdapter {
    private LayoutInflater inflater;

    public RecommendationsAdapter(Context context, List<TaggedEvent> items) {
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

        if (getItem(position).isPersonal()) {
            text.setText(getContext().getString(R.string.recommendations_title_personal));
        } else {
            text.setText(getContext().getString(R.string.recommendations_title_popular));
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

    public static class TaggedEvent extends TaggedReference<Event, Boolean> implements IdEquals<TaggedEvent> {

        public TaggedEvent(Event event) {
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
}

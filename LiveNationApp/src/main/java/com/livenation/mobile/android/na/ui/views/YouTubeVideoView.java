package com.livenation.mobile.android.na.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.youtube.YouTubeVideo;

public class YouTubeVideoView extends LinearLayout {
    private NetworkImageView imageView;
    private TextView title;

    public YouTubeVideoView(Context context) {
        super(context);
        initialize();
    }

    public YouTubeVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public YouTubeVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize();
    }


    public void displayVideo(YouTubeVideo video) {
        title.setText(video.getTitle());
        if (video.getThumbnailURLs().isEmpty()) {
            imageView.setImageUrl(null, getImageLoader());
        } else {
            imageView.setImageUrl(video.getThumbnailURLs().get(0), getImageLoader());
        }
    }

    public ImageLoader getImageLoader() {
        return LiveNationApplication.get().getImageLoader();
    }


    private void initialize() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_youtube_video, isInEditMode() ? null : this, false);

        this.imageView = (NetworkImageView) view.findViewById(R.id.view_youtube_video_image);
        this.title = (TextView) view.findViewById(R.id.view_youtube_video_title);

        addView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }
}

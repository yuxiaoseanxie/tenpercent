package com.livenation.mobile.android.na.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.presenters.SingleArtistPresenter;
import com.livenation.mobile.android.na.presenters.views.SingleArtistView;
import com.livenation.mobile.android.na.ui.support.LiveNationFragment;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.Artist;

public class ArtistFragment extends LiveNationFragment implements SingleArtistView {
    private final static String[] IMAGE_PREFERRED_ARTIST_KEYS = {"mobile_detail", "tap"};

    private NetworkImageView artistImageView;
    private TextView artistTitle;

    //region Lifecycle

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);

        this.artistImageView = (NetworkImageView)view.findViewById(R.id.fragment_show_image);
        this.artistTitle = (TextView)view.findViewById(R.id.fragment_show_artist_title);

        getSingleArtistPresenter().initialize(getActivity(), getActivity().getIntent().getExtras(), this);

        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        getSingleArtistPresenter().cancel(this);
    }

    //endregion


    //region Presenter

    @Override
    public void setSingleArtist(Artist artist) {
        artistTitle.setText(artist.getName());

        String imageKey = artist.getBestImageKey(IMAGE_PREFERRED_ARTIST_KEYS);
        if(imageKey != null) {
            String imageUrl = artist.getImageURL(imageKey);
            artistImageView.setImageUrl(imageUrl, getImageLoader());
        }
    }


    private void init() {
        getSingleArtistPresenter().initialize(getActivity(), getActivity().getIntent().getExtras(), this);
    }

    private void deinit() {
        getSingleArtistPresenter().cancel(this);
    }

    private SingleArtistPresenter getSingleArtistPresenter() {
        return LiveNationApplication.get().getSingleArtistPresenter();
    }

    //endregion
}

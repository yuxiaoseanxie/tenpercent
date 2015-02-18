/*
 * 
 * @author Charlie Chilton 2014/02/07
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.ui.support;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;

import android.os.Bundle;
import android.view.View;

/**
 * This class exists because of this:
 * <p/>
 * http://stackoverflow.com/questions/14047257/how-do-i-know-the-map-is-ready-to-get-used-when-using-the-supportmapfragment
 * <p/>
 * While its possible just to keep the map in XML, having explicit control over what fragmentManager it gets assigned to
 * is more fun.
 *
 * @author cchilton
 */
public class LiveNationMapFragment extends SupportMapFragment {
    private MapReadyListener listener;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (null != listener) {
            listener.onMapReady(getMap());
        }
    }

    public void setMapReadyListener(MapReadyListener listener) {
        this.listener = listener;
    }

    public static interface MapReadyListener {
        void onMapReady(GoogleMap map);
    }

}

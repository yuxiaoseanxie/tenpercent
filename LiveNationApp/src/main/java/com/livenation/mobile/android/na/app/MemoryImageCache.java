/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.livenation.mobile.android.na.analytics.LiveNationAnalytics;

/**
 * Simple LRU in memory cache
 *
 * @author cchilton
 *         <p/>
 *         2013/01/17
 */
public class MemoryImageCache extends LruCache<String, Bitmap> implements ImageCache {

    public MemoryImageCache(int maxSize) {
        super(maxSize);
    }

    public static int getDefaultLruSize() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/20th (5%) of the available memory for this memory cache.
        final int cacheSize = maxMemory / 20;

        return cacheSize;
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        LiveNationAnalytics.logTrace("MemoryImageCache", "putBitmap(" + String.valueOf(sizeOf(url, bitmap)) + "), max cache Size: " + String.valueOf(maxSize()) + " current cache size: " + String.valueOf(size()));
        put(url, bitmap);
    }

}

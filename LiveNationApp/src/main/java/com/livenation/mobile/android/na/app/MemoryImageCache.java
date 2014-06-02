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
        //TODO:Calculate based on device RAM
        return 4 * 1024 * 1024; //4 MiB
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

}

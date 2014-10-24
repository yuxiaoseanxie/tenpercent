package com.livenation.mobile.android.na.helpers;

import android.content.Context;
import android.content.res.TypedArray;

import com.livenation.mobile.android.na.R;

import java.util.Random;

/**
 * Created by cchilton on 5/28/14.
 */
public class DefaultImageHelper {
    private static int[] defaultDpImages;
    private static int[] defaultTapImages;

    private static void initializeDefaultTapImages(Context context) {
        TypedArray defaultImageArray = context.getResources().obtainTypedArray(R.array.hero_tap_images);
        /** This is required for caching purpose. When you call recycle it means that this object
         * can be reused from this point. Internally TypedArray contains few arrays so in order
         * not to allocate memory each time when TypedArray is used it is cached in Resources
         * class as static field
         */
        defaultImageArray.recycle();
        int size = defaultImageArray.length();
        defaultTapImages = new int[size];
        for (int i = 0; i < size; i++) {
            int resourceId = Integer.valueOf(defaultImageArray.getResourceId(i, -1));
            defaultTapImages[i] = resourceId;
        }
    }

    private static void initializeDefaultDpImages(Context context) {
        TypedArray defaultImageArray = context.getResources().obtainTypedArray(R.array.hero_images);
        defaultImageArray.recycle();
        int size = defaultImageArray.length();
        defaultDpImages = new int[size];
        for (int i = 0; i < size; i++) {
            int resourceId = Integer.valueOf(defaultImageArray.getResourceId(i, -1));
            defaultDpImages[i] = resourceId;
        }
    }

    public static int computeDefaultTapDrawableId(Context context, long randomSeed) {
        if (defaultTapImages == null) initializeDefaultTapImages(context);

        int index = new Random(randomSeed).nextInt(defaultTapImages.length);
        return defaultTapImages[index];
    }

    public static int computeDefaultDpDrawableId(Context context, long randomSeed) {
        if (defaultDpImages == null) initializeDefaultDpImages(context);
        int index = new Random(randomSeed).nextInt(defaultDpImages.length);
        return defaultDpImages[index];
    }

}

package com.livenation.mobile.android.na.helpers;

/**
 * Created by cchilton on 3/25/14.
 *
 * A wrapper class to wrap some object and then provide a tagging mechanism.
 *
 * It's usage/design/philosophy has been modeled on the usage of WeakReference<T>
 *    
 */
public class TaggedReference<TReference, TTag> {
    private final TReference target;
    private TTag tag;

    public TaggedReference(TReference target) {
        this.target = target;
    }

    public TReference get() {
        return target;
    }

    public TTag getTag() {
        return tag;
    }

    public void setTag(TTag tag) {
        this.tag = tag;
    }
}

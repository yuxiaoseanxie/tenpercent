package com.livenation.mobile.android.na.helpers;

import android.content.Context;

public interface PersistenceProvider<T> {
    void write(String key, T value, Context context);

    T read(String key, Context context);

    boolean remove(String key, Context context);

    boolean reset(Context context);
}

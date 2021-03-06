/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.support;

import android.content.Context;
import android.os.Bundle;

public interface Presenter<T extends PresenterView> {
    void initialize(Context context, Bundle args, T view);

    void cancel(T view);
}

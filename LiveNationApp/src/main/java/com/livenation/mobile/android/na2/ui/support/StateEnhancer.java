/*
 * 
 * @author Charlie Chilton 2014/02/06
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na2.ui.support;

import android.os.Bundle;
/**
 * This interface exists to assist persisting View states in fragments.
 * 
 * The inbuilt android lifecycle does not have clean support for state persistence between the..
 * 
 * onDestroyView() -> onCreateView() section of the Fragment Lifecycle.
 * 
 * 
 * @author cchilton
 *
 */
public interface StateEnhancer {
	public void applyInstanceState(Bundle state);
}

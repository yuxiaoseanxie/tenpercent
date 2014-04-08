/*
 * 
 * @author Charlie Chilton 2014/01/22
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.presenters.views;

import com.livenation.mobile.android.na.presenters.support.PresenterView;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.RecommendationSet;

import java.util.List;

public interface RecommendationSetsView extends PresenterView {
    void setRecommendationSets(List<RecommendationSet> recommendationSets);
}

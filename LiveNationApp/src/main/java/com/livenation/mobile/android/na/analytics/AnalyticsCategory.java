package com.livenation.mobile.android.na.analytics;

/**
 * Created by elodieferrais on 5/20/14.
 */
public enum AnalyticsCategory {
    ON_BOARDING(AnalyticConstants.CATEGORY_ONBOARDING_VALUE),
    ACTION_BAR(AnalyticConstants.CATEGORY_ACTION_BAR_VALUE),
    DRAWER(AnalyticConstants.CATEGORY_DRAWER_VALUE),
    RECOMMENDATIONS(AnalyticConstants.CATEGROY_RECOMMENDATIONS_VALUE),
    ALL_SHOWS(AnalyticConstants.CATEGROY_ALL_SHOWS_VALUE),
    NEARBY(AnalyticConstants.CATEGROY_NEARBY_VALUE),
    HOME_SCREEN(AnalyticConstants.CATEGORY_HOME_SCREEN_VALUE),
    ADP(AnalyticConstants.CATEGORY_ADP_VALUE),
    SDP(AnalyticConstants.CATEGORY_SDP_VALUE),
    VDP(AnalyticConstants.CATEGORY_VDP_VALUE),
    FAVORITES(AnalyticConstants.CATEGORY_FAVORITES_VALUE),
    SEARCH(AnalyticConstants.CATEGORY_SEARCH_VALUE),
    LEGAL(AnalyticConstants.CATEGORY_LEGAL_VALUE),
    NOTIFICATION(AnalyticConstants.CATEGORY_NOTIFICATION_VALUE),
    LOCATION(AnalyticConstants.CATEGORY_LOCATION_VALUE),
    HOUSEKEEPING(AnalyticConstants.CATEGORY_HOUSEKEEPING_VALUE),
    ERROR(AnalyticConstants.CATEGORY_ERROR_VALUE),
    UNKNOWN(AnalyticConstants.CATEGORY_UNKNOWN);


    public String categoryName;

    AnalyticsCategory(String categoryName) {
        this.categoryName = categoryName;
    }

}

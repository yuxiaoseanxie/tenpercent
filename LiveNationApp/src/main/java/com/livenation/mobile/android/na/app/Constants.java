/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

public class Constants {

    public static final float METERS_IN_A_MILE = 1609.34f;

    //arbitary delay between user keypresses and sending user text to the API
    public static final int TEXT_CHANGED_POST_DELAY = 667;

    public class Notifications {
        public static final String EXTRA_TYPE = "type";

        public static final String TYPE_PUSH_CAPTCHA = "silent_push";
        public static final int TYPE_FEATURED_CONTENT = -1;
        public static final int TYPE_EVENT_ON_SALE_NOW = 0;
        public static final int TYPE_EVENT_ANNOUNCEMENT = 1;
        public static final int TYPE_EVENT_LAST_MINUTE = 2;
        public static final int TYPE_EVENT_MOBILE_PRESALE = 3;
        public static final int TYPE_IN_VENUE = 4;


        public static final String EXTRA_ENTITY_ID = "id";
        public static final String EXTRA_PUSH_CAPTCHA_PAYLOAD = "payload";

        public static final String EXTRA_MESSAGE_ACTION_URL = "message_action_url";
        public static final String EXTRA_MESSAGE_ACTION_NAME = "message_action_name";

        public static final String EXTRA_IMAGE_URL = "image_url";
        public static final String EXTRA_VIDEO_URL = "video_url";

        public static final String EXTRA_EVENT_INFO_ON_SALE_DATE = "on_sale_date";
        public static final String EXTRA_EVENT_INFO_ARTIST_NAME = "artist_name";
        public static final String EXTRA_EVENT_INFO_VENUE_NAME = "venue_name";
        public static final String EXTRA_EVENT_INFO_LOCAL_START_TIME = "local_start_time";

        public static final String EXTRA_RICH_MESSAGE_ID = "_uamid";
        public static final String EXTRA_RICH_MESSAGE_VALUE = "com.urbanairship.push.ALERT";
    }

    public class SharedPreferences {
        public static final String DEBUG_MODE_DATA = "debug_mode_data";
        public static final String DEBUG_MODE_IS_DEBUG_MODE_ACTIVATED = "debug_mode_is_debug_mode_activated";
        public static final String TICKETING_ENVIRONMENT = "ticketing_environment";
        public static final String NOTIFICATIONS_SAVED_APID = "notifications_saved_apid";
        public static final String AB_TESTING = "ab_testing";

        public static final String ON_BOARDING_NAME = "on_boarding_has_been_displayed";
        public static final String ON_BOARDING_HAS_BEEN_DISPLAYED = "on_boarding_has_been_displayed";
        //1.X key
        public static final String USER_ALLOWS_MEDIA_SCRAPE = "USER_ALLOWS_MEDIA_SCRAPE";

        //Rating
        public static final String RATING_DIALOG_CLICKED = "rating_dialog_clicked";

        //AB-testing
        public static final String IS_NEW_USER = "is_new_user";

        //Config File
        public static final String MINIMUM_CHECKOUT_VERSION = "checkout_requires";
        public static final String UPGRADE_MAXIMUM_VERSION = "upgrade_maximum_version";
        public static final String UPGRADE_MESSAGE = "upgrade_message";
        public static final String UPGRADE_PLAY_STORE_LINK = "upgrade_play_store_link";
        public static final String FEATURED_CAROUSEL_CHART = "featured_carousel_chart";
        public static final String UBER_FREE_RIDE_TEXT = "uber_free_ride_text";
        public static final String CONFIRMATION_ACTIONS = "confirmation_actions";

    }

}

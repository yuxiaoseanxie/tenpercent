/*
 * 
 * @author Charlie Chilton 2014/01/16
 * 
 * Copyright (C) 2014 Live Nation Labs. All rights reserved.
 * 
 */

package com.livenation.mobile.android.na.app;

import java.util.UUID;

public class Constants {
	public static final String clientId = "9e34bce8-5ea3-11e3-a9e5-5a59eb79d850";

    public static enum Environment {
        Production("https://api.livenation.com", "0361b9ba-9381-11e3-bc4c-bad30bc9cabe"),
        StagingDirect("https://stg-faceoff.herokuapp.com", "9e34bce8-5ea3-11e3-a9e5-5a59eb79d850"),
        StagingAkamai("http://stg.api.livenation.com", "9e34bce8-5ea3-11e3-a9e5-5a59eb79d850"),
        Integration("https://int-faceoff.herokuapp.com", "486e2ff6-98f5-11e3-b37c-ca4ec9c3f32c");

        private final String host;
        private final String clientId;

        Environment(String host, String clientId) {
            this.host = host;
            this.clientId = clientId;
        }

        public String getClientId() {
            return clientId;
        }

        public String getHost() {
            return host;
        }
    }

    public class Notifications {
        public static final String EXTRA_TYPE = "type";

        public static final int TYPE_FEATURED_CONTENT = -1;
        public static final int TYPE_EVENT_ON_SALE_NOW = 0;
        public static final int TYPE_EVENT_ANNOUNCEMENT = 1;
        public static final int TYPE_EVENT_LAST_MINUTE = 2;
        public static final int TYPE_EVENT_MOBILE_PRESALE = 3;
        public static final int TYPE_IN_VENUE = 4;


        public static final String EXTRA_ENTITY_ID = "id";

        public static final String EXTRA_MESSAGE_ACTION_URL = "message_action_url";
        public static final String EXTRA_MESSAGE_ACTION_NAME = "message_action_name";

        public static final String EXTRA_IMAGE_URL = "image_url";
        public static final String EXTRA_VIDEO_URL = "video_url";

        public static final String EXTRA_EVENT_INFO_ON_SALE_DATE = "on_sale_date";
        public static final String EXTRA_EVENT_INFO_ARTIST_NAME = "artist_name";
        public static final String EXTRA_EVENT_INFO_VENUE_NAME = "venue_name";
        public static final String EXTRA_EVENT_INFO_LOCAL_START_TIME = "local_start_time";

        public static final String EXTRA_RICH_MESSAGE_ID = "_uamid";
    }
}

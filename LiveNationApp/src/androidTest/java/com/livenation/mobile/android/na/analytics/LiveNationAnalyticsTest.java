package com.livenation.mobile.android.na.analytics;

import android.test.InstrumentationTestCase;

/**
 * Created by cchilton on 1/13/15.
 */
public class LiveNationAnalyticsTest extends InstrumentationTestCase {
    private final String EVENT_TITLE1 = "event_title_1";
    private final String EVENT_CATEGORY1 = "event_category_1";
    private final String SCREEN_TITLE1 = "screen_title_1";
    private final String PROP_KEY1 = "key1";
    private final String PROP_KEY2 = "key2";
    private final String PROP_KEY3 = "key3";
    private final String PROP_VALUE1 = "key1";
    private final String PROP_VALUE2 = "key2";
    private final int PROP_VALUE3 = 3;
    private Props props = new Props();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        props.put(PROP_KEY1, PROP_VALUE1);
        props.put(PROP_KEY2, PROP_VALUE2);
        props.put(PROP_KEY3, PROP_VALUE3);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        LiveNationAnalytics.initialize();
    }

    public void testTrack() {
        LiveNationAnalytics.initialize(new MockAnalyticService(new MockAnalyticService.OnAnalytics() {
            @Override
            public void onScreen(String screenTitle, Props props) {
                fail();
            }

            @Override
            public void onTrack(String eventTitle, Props props) {
                assertEquals(eventTitle, EVENT_TITLE1 + AnalyticConstants.PLATFORM_EVENT_SUFFIX);
                assertEquals(props.keySet().size(), 5);
                assertEquals(props.get(AnalyticConstants.CATEGORY), EVENT_CATEGORY1);
                assertEquals(props.get(AnalyticConstants.PLATFORM), AnalyticConstants.PLATFORM_VALUE);

                assertEquals(props.get(PROP_KEY1), PROP_VALUE1);
                assertEquals(props.get(PROP_KEY2), PROP_VALUE2);
                assertEquals(props.get(PROP_KEY3), PROP_VALUE3);
            }
        }));

        LiveNationAnalytics.track(EVENT_TITLE1, EVENT_CATEGORY1, props);
    }

    public void testScreen() {
        LiveNationAnalytics.initialize(new MockAnalyticService(new MockAnalyticService.OnAnalytics() {
            @Override
            public void onScreen(String screenTitle, Props props) {
                assertEquals(screenTitle, SCREEN_TITLE1 + AnalyticConstants.PLATFORM_EVENT_SUFFIX);

                assertEquals(props.keySet().size(), 4);
                assertEquals(props.get(AnalyticConstants.PLATFORM), AnalyticConstants.PLATFORM_VALUE);

                assertEquals(props.get(PROP_KEY1), PROP_VALUE1);
                assertEquals(props.get(PROP_KEY2), PROP_VALUE2);
                assertEquals(props.get(PROP_KEY3), PROP_VALUE3);
            }

            @Override
            public void onTrack(String eventTitle, Props props) {
                fail();
            }
        }));

        LiveNationAnalytics.screen(SCREEN_TITLE1, props);
    }
}

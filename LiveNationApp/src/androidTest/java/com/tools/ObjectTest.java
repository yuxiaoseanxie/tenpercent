package com.tools;

import org.json.JSONObject;

import android.content.Context;

/**
 * Created by elodieferrais on 1/13/15.
 */
public class ObjectTest {

    public static JSONObject getConfigFileSample(Context context) {
        JSONObject json = SampleJsonReader.getJsonObjectFromAssets(context, "config-file-response.json");
        return json;
    }
}

package com.tools;

import android.content.Context;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by elodieferrais on 1/9/15.
 */
public class SampleJsonReader {

    public static String loadJSONFromAsset(Context context, String fileName) {

        String json = null;
        try {

            InputStream is = context.getAssets().open(fileName);

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            Assert.fail("Exception during the json parsing: " + ex.getMessage());
            return null;
        }
        return json;
    }

    public static JSONObject getJsonObjectFromAssets(Context context, String fileName) {
        String json = loadJSONFromAsset(context, fileName);
        try {
            JSONObject jsonObject = new JSONObject(json);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

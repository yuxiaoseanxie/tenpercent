package com.tools;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.livenation.mobile.android.platform.api.service.livenation.impl.processor.GenericProcessor;

import junit.framework.Assert;

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
}

package stubs.matchers;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonObjectBodyMatcher implements BodyMatcher {
    private final JSONObject toMatch;

    public JsonObjectBodyMatcher(@NonNull JSONObject toMatch) {
        this.toMatch = toMatch;
    }


    @Override
    public boolean matches(byte[] rawBody) {
        try {
            String responseString = new String(rawBody);
            JSONObject responseJson = new JSONObject(responseString);
            if (responseJson.length() != toMatch.length())
                return false;

            Iterator<String> keys = toMatch.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!toMatch.get(key).equals(responseJson.opt(key)))
                    return false;
            }

            return true;
        } catch (JSONException ignored) {
        }
        return false;
    }
}

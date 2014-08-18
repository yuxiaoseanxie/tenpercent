package stubs.matchers;

import android.support.annotation.NonNull;

public class StringBodyMatcher implements BodyMatcher {
    private final String toMatch;

    public StringBodyMatcher(@NonNull String toMatch) {
        this.toMatch = toMatch;
    }

    @Override
    public boolean matches(byte[] rawBody) {
        String responseString = new String(rawBody);
        return toMatch.equals(responseString);
    }
}

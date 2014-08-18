package stubs.matchers;

public interface BodyMatcher {
    boolean matches(byte[] rawBody);

    public static final BodyMatcher ANY = new BodyMatcher() {
        @Override
        public boolean matches(byte[] rawBody) {
            return true;
        }
    };
}

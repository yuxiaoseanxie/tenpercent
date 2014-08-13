package stubs.converters;

import android.support.annotation.Nullable;

public interface JsonConverter {
    String convertToJsonString(@Nullable Object object) throws Exception;
}

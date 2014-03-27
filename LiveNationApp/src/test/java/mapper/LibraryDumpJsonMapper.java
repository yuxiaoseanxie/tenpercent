package mapper;

import android.test.InstrumentationTestCase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryDump;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.LibraryEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by elodieferrais on 3/26/14.
 */
public class LibraryDumpJsonMapper  extends InstrumentationTestCase {

    public void testLibraryDumpJsonMapperOnSuccess() {
        LibraryEntry libraryEntry = new LibraryEntry("U2");
        libraryEntry.setPlayCount(2);
        libraryEntry.setTotalSongs(3);
        LibraryDump libraryDump = new LibraryDump();
        libraryDump.add(libraryEntry);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String output = objectMapper.writeValueAsString(libraryDump);

            JSONObject outputJson = new JSONObject(output);
            assertNotNull(outputJson);

            JSONArray outputJsonArray = outputJson.getJSONArray("data");
            assertNotNull(outputJsonArray);
            assertEquals(outputJsonArray.length(), 1);

            JSONObject libraryEntryJson = outputJsonArray.getJSONObject(0);
            String name = libraryEntryJson.getString("name");
            assertNotNull(name);
            assertEquals(name, "U2");

            int playCount = libraryEntryJson.getInt("playCount");
            assertNotNull(playCount);
            assertEquals(playCount, 2);

            int totalSongs = libraryEntryJson.getInt("totalSongs");
            assertNotNull(totalSongs);
            assertEquals(totalSongs, 3);

        } catch (JsonProcessingException e) {
            fail();
        } catch (JSONException e) {
            fail();
        }
    }
}

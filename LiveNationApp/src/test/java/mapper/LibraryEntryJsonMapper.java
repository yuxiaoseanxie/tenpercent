package mapper;

import android.test.InstrumentationTestCase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.MusicLibraryEntry;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by elodieferrais on 3/26/14.
 */
public class LibraryEntryJsonMapper extends InstrumentationTestCase {

    public void testLibraryEntryJsonMapperOnSuccess() {
        MusicLibraryEntry musicLibraryEntry = new MusicLibraryEntry("U2");
        musicLibraryEntry.setPlayCount(2);
        musicLibraryEntry.setTotalSongs(3);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String output = objectMapper.writeValueAsString(musicLibraryEntry);
            JSONObject outputJson = new JSONObject(output);
            String name = outputJson.getString("name");
            assertNotNull(name);
            assertEquals(name, "U2");

            int playCount = outputJson.getInt("playCount");
            assertNotNull(playCount);
            assertEquals(playCount, 2);

            int totalSongs = outputJson.getInt("totalSongs");
            assertNotNull(totalSongs);
            assertEquals(totalSongs, 3);

        } catch (JsonProcessingException e) {
            fail();
        } catch (JSONException e) {
            fail();
        }
    }
}

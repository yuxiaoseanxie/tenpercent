package com.livenation.mobile.android.na.ui;

import android.os.Bundle;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.adapters.HelpListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elodieferrais on 4/28/14.
 */
public class HelpMenuActivity extends LiveNationFragmentActivity implements Response.ErrorListener, Response.Listener<JSONObject> {

    private final static String TOPICS_KEY = "topics";
    private final static String SLUG_KEY = "slug";
    private final static String NAME_KEY = "name";

    private final static String DESCRIPTION_KEY = "description";

    private RequestQueue queue;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);

        listView = new ListView(this);
        setContentView(listView);

        loadHelpJson();

    }

    private void loadHelpJson() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, getString(R.string.help_url_json), null, this, this);
        queue.add(request);
    }

    private void buildList(JSONArray items) {
        List<HelpMenuItem> itemList = new ArrayList<HelpMenuItem>(items.length());
        try {
            for (int i = 0; i < items.length(); i++) {

                JSONObject item = items.getJSONObject(i);
                String slug = item.getString(SLUG_KEY);
                String name = item.getString(NAME_KEY);
                String description = item.getString(DESCRIPTION_KEY);

                HelpMenuItem helpMenuItem = new HelpMenuItem(slug, name, description);
                itemList.add(helpMenuItem);
            }

            HelpListAdapter adapter = new HelpListAdapter(this, itemList);
            listView.setAdapter(adapter);


        } catch (JSONException e) {
            onErrorResponse(new VolleyError(e));
        }
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        //TODO https://www.pivotaltracker.com/story/show/70329394
    }

    @Override
    public void onResponse(JSONObject response) {
        try {
            JSONArray helpMenuItem = response.getJSONArray(TOPICS_KEY);
            buildList(helpMenuItem);

        } catch (JSONException e) {
            onErrorResponse(new VolleyError(e));
        }
    }

    public class HelpMenuItem {
        public final String slug;
        public final String name;
        public final String description;

        private HelpMenuItem(String slug, String name, String description) {
            this.slug = slug;
            this.name = name;
            this.description = description;
        }
    }
}

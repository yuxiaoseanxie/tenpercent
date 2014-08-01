package ui;

import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import com.livenation.mobile.android.na.R;
import com.livenation.mobile.android.na.ui.HomeActivity;
import com.livenation.mobile.android.platform.api.service.livenation.impl.model.SearchResult;

import static org.hamcrest.Matchers.*;

import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.*;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.*;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.*;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.*;

public class HomeActivityTests extends ActivityInstrumentationTestCase2<HomeActivity> {
    public HomeActivityTests() {
        super(HomeActivity.class);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        getActivity();
    }


    /*
    public void testSearchArtist() throws UiObjectNotFoundException, InterruptedException {
        openApp();
        UiObject searchButton =
                new UiObject(new UiSelector().description("Search"));
        searchButton.clickAndWaitForNewWindow();
        UiObject editText =
                new UiObject(new UiSelector().className("android.widget.EditText"));
        editText.setText("moby");
        getUiDevice().pressEnter();
        UiScrollable results =
                new UiScrollable(
                        new UiSelector().className("android.widget.ListView"));

        UiObject firstResult = results.getChild((new UiSelector().text("Moby")));
        firstResult.clickAndWaitForNewWindow();
    }
    */
    public void testSearchArtist() {
        onView(withId(R.string.home_menu_search))
                .perform(click());

        onView(withId(R.id.view_search_actionbar_input))
                .perform(typeText("Moby"))
                .perform(pressKey(KeyEvent.KEYCODE_ENTER));

        onData(is(SearchResult.class))
                .atPosition(0)
                .perform(click());
    }
}

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;

public class HomeActivityTests extends
        com.android.uiautomator.testrunner.UiAutomatorTestCase {
    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
    }

    private void openApp() throws UiObjectNotFoundException {
        getUiDevice().pressHome();
        UiObject allAppsButton = new UiObject(
                new UiSelector().description("Apps"));
        allAppsButton.clickAndWaitForNewWindow();

        UiScrollable appViews = new UiScrollable(
                new UiSelector().scrollable(true));
        appViews.setAsHorizontalList();
        UiObject ourApp = appViews.getChildByText(new UiSelector()
                        .className(android.widget.TextView.class.getName()),
                "Live Nation"
        );
        ourApp.clickAndWaitForNewWindow();
        UiObject appValidation = new UiObject(
                new UiSelector().packageName("com.livenation.mobile.android.na"));
        assertTrue("Could not open test app", appValidation.exists());
    }

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


    @Override
    protected void tearDown() throws Exception {
        // TODO Auto-generated method stub
        super.tearDown();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
        getUiDevice().pressBack();
    }
}
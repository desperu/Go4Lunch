package org.desperu.go4lunch.viewmodel;

import org.desperu.go4lunch.view.main.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class AutocompleteLiveDataTest {

    private ActivityController<MainActivity> controller;
    private MainActivity activity;

    private void createMainActivityWithIntent() {
        activity = controller
                .create()
                .start()
                .get();
    }

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(MainActivity.class);
    }

    @Test
    public void testPlacesIdListLiveData() {
        ArrayList<String> placesIdList = new ArrayList<>();
        createMainActivityWithIntent();

        AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(activity.getApplication());
        autocompleteViewModel.setPlacesIdListLiveData(placesIdList);

        autocompleteViewModel.getPlacesIdListLiveData().observeForever(placesIdList1 -> {
            assertNotNull(placesIdList1);
            assertEquals(placesIdList, placesIdList1);
        });
    }
}
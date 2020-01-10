package org.desperu.go4lunch.viewmodel;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class AutocompleteLiveDataTest {

    @Test
    public void testPlacesIdListLiveData() {
        ArrayList<String> placesIdList = new ArrayList<>();

        AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(ApplicationProvider.getApplicationContext());
        autocompleteViewModel.setPlacesIdListLiveData(placesIdList);

        autocompleteViewModel.getPlacesIdListLiveData().observeForever(placesIdList1 -> {
            assertNotNull(placesIdList1);
            assertEquals(placesIdList, placesIdList1);
        });
    }
}
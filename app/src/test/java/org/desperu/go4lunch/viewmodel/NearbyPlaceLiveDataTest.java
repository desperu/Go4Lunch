package org.desperu.go4lunch.viewmodel;

import androidx.test.core.app.ApplicationProvider;

import com.google.android.libraries.places.api.model.Place;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class NearbyPlaceLiveDataTest {

    @Test
    public void testPlacesListLiveData() {
        ArrayList<Place> placesList = new ArrayList<>();

        NearbyPlaceViewModel nearbyPlaceViewModel = new NearbyPlaceViewModel(ApplicationProvider.getApplicationContext());
        nearbyPlaceViewModel.setPlacesList(placesList);

        nearbyPlaceViewModel.getPlacesList().observeForever(placesList1 -> {
            assertNotNull(placesList1);
            assertEquals(placesList, placesList1);
        });
    }
}
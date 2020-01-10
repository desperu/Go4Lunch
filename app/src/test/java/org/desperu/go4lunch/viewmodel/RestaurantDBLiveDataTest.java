package org.desperu.go4lunch.viewmodel;

import androidx.test.core.app.ApplicationProvider;

import org.desperu.go4lunch.models.Restaurant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class RestaurantDBLiveDataTest {

    @Test
    public void testRestaurantLiveData() {
        Restaurant restaurant = new Restaurant();

        RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(
                ApplicationProvider.getApplicationContext(), "test");
        restaurantDBViewModel.setRestaurantLiveData(restaurant);

        restaurantDBViewModel.getRestaurantLiveData().observeForever(restaurant1 -> {
            assertNotNull(restaurant1);
            assertEquals(restaurant, restaurant1);
        });
    }
}
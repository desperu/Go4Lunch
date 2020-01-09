package org.desperu.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.view.main.MainActivity;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class RestaurantInfoLiveDataTest {

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
    public void testPlaceLiveData() throws InterruptedException {
        Place place = Place.builder().build();
        createMainActivityWithIntent();

        RestaurantInfoViewModel restaurantInfoViewModel = new RestaurantInfoViewModel(activity.getApplication(), "test");
        restaurantInfoViewModel.setPlaceMutableLiveData(place);

        restaurantInfoViewModel.getPlaceLiveData().observeForever(place1 -> {
            assertNotNull(place1);
            assertEquals(place, place1);
        });

        assertEquals(getOrAwaitValue(2, restaurantInfoViewModel.getPlaceLiveData()), place);
    }

    @SuppressWarnings("unchecked")
    private Object getOrAwaitValue(long time, @NotNull LiveData<Place> liveData) throws InterruptedException {
        TimeUnit timeUnit = TimeUnit.SECONDS;
        final Object[] data = {null};
        CountDownLatch latch = new CountDownLatch(1);
        Observer observer = (new Observer() {
            @Override
            public void onChanged(Object o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        });

        liveData.observeForever(observer);

        // Don't wait indefinitely if the LiveData is not set.
        if (!latch.await(time, timeUnit)) {
            throw new InterruptedException("LiveData value was never set.");
        }

        return data[0];
    }
}
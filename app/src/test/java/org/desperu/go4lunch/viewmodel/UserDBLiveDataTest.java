package org.desperu.go4lunch.viewmodel;

import androidx.test.core.app.ApplicationProvider;

import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.view.main.MainActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest=Config.NONE)
public class UserDBLiveDataTest {

    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private UserDBViewModel userDBViewModel;

    private void createMainActivityWithIntent() {
        activity = controller
                .create()
                .start()
                .get();
    }

    @Before
    public void setUp() {
//        controller = Robolectric.buildActivity(MainActivity.class);
//        createMainActivityWithIntent();
        userDBViewModel = new UserDBViewModel(ApplicationProvider.getApplicationContext(), "uid");
    }

    @Test
    public void testUserLiveData() {
        User user = new User();
        userDBViewModel.setUserLiveData(user);

        userDBViewModel.getUserLiveData().observeForever(user1 -> {
            assertNotNull(user1);
            assertEquals(user, user1);
        });
    }

    @Test
    public void testAllUsersListLiveData() {
        List<User> allUsersList = new ArrayList<>();
        userDBViewModel.setAllUsersList(allUsersList);

        userDBViewModel.getAllUsersListLiveData().observeForever(allUsersList1 -> {
            assertNotNull(allUsersList1);
            assertEquals(allUsersList, allUsersList1);
        });
    }

    @Test
    public void testBookedResponseLiveData() {
        int bookedResponse = 1;
        userDBViewModel.setUpdateBookedResponse(bookedResponse);

        userDBViewModel.getUpdateBookedResponse().observeForever(bookedResponse1 -> {
            assertNotNull(bookedResponse1);
            assertEquals(0, bookedResponse - bookedResponse1);
        });
    }
}
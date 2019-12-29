package org.desperu.go4lunch.models;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class UserTest {

    private String uid = "user uid";
    private String userName = "user name";
    private String urlPicture = "url picture";
    private String bookedRestaurantId = "booked restaurant id";

    @Test
    public void Given_userWithoutData_When_createUser_Then_noUserData() {
        User user = new User();

        assertThat("No user uid", user.getUid() == null);
        assertThat("No user name", user.getUserName() == null);
        assertThat("No user url picture", user.getUrlPicture() == null);
        assertThat("No user booked restaurant id", user.getBookedRestaurantId() == null);
    }

    @Test
    public void Given_userWithData_When_createUser_Then_checkUserData() {
        User user = new User(uid, userName, urlPicture, bookedRestaurantId);

        assertEquals(uid, user.getUid());
        assertEquals(userName, user.getUserName());
        assertEquals(urlPicture, user.getUrlPicture());
        assertEquals(bookedRestaurantId, user.getBookedRestaurantId());
    }

    @Test
    public void Given_userWithoutData_When_useUserSetters_Then_checkUserData() {
        User user = new User();

        user.setUid(uid);
        user.setUserName(userName);
        user.setUrlPicture(urlPicture);
        user.setBookedRestaurantId(bookedRestaurantId);

        assertEquals(uid, user.getUid());
        assertEquals(userName, user.getUserName());
        assertEquals(urlPicture, user.getUrlPicture());
        assertEquals(bookedRestaurantId, user.getBookedRestaurantId());
    }
}
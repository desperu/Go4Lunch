package org.desperu.go4lunch.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class RestaurantTest {

    private String name = "restaurantName";
    private String restaurantId = "restaurantId";
    private List<String> bookedUsersId = new ArrayList<>();
    private Double stars = 4.5;
    private List<String> likeUsers = new ArrayList<>();

    @Before
    public void before() {
        bookedUsersId.add("user1");
        bookedUsersId.add("user2");
        bookedUsersId.add("user3");

        likeUsers.add("user1");
        likeUsers.add("user2");
        likeUsers.add("user3");
    }

    @Test
    public void Given_restaurantWithoutData_When_createRestaurant_Then_noRestaurantData() {
        Restaurant restaurant = new Restaurant();

        assertThat("No restaurant name", restaurant.getName() == null);
        assertThat("No restaurant id", restaurant.getRestaurantId() == null);
        assertThat("No restaurant booked users id", restaurant.getBookedUsersId() == null);
        assertThat("No restaurant stars", restaurant.getStars() == null);
        assertThat("No restaurant like users", restaurant.getLikeUsers() == null);
    }

    @Test
    public void Given_restaurantWithData_When_createRestaurant_Then_checkRestaurantData() {
        Restaurant restaurant = new Restaurant(name, restaurantId, bookedUsersId, stars, likeUsers);

        assertEquals(name, restaurant.getName());
        assertEquals(restaurantId, restaurant.getRestaurantId());
        assertEquals(bookedUsersId, restaurant.getBookedUsersId());
        assertEquals(stars, restaurant.getStars());
        assertEquals(likeUsers, restaurant.getLikeUsers());
    }

    @Test
    public void Given_restaurantWithoutData_When_useRestaurantSetters_Then_checkRestaurantData() {
        Restaurant restaurant = new Restaurant();

        restaurant.setName(name);
        restaurant.setRestaurantId(restaurantId);
        restaurant.setBookedUsersId(bookedUsersId);
        restaurant.setStars(stars);
        restaurant.setLikeUsers(likeUsers);

        assertEquals(name, restaurant.getName());
        assertEquals(restaurantId, restaurant.getRestaurantId());
        assertEquals(bookedUsersId, restaurant.getBookedUsersId());
        assertEquals(stars, restaurant.getStars());
        assertEquals(likeUsers, restaurant.getLikeUsers());
    }
}
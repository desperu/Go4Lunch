package org.desperu.go4lunch.viewmodel;

import androidx.databinding.ObservableField;

import org.desperu.go4lunch.api.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;

public class RestaurantDBViewModel {

    private String restaurantId;
    private RestaurantDetailActivity restaurantDetailActivity;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();

    public RestaurantDBViewModel(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public RestaurantDBViewModel(RestaurantDetailActivity restaurantDetailActivity, String restaurantId) {
        this.restaurantDetailActivity = restaurantDetailActivity;
        this.restaurantId = restaurantId;
    }

    // --------------
    // REQUEST
    // --------------

    public void fetchRestaurant() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot ->
                restaurant.set(documentSnapshot.toObject(Restaurant.class)));
    }

    /**
     * Get restaurants booked users.
     */
    public void getRestaurantBookedUsers() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot ->
                restaurantDetailActivity.updateRecyclerView(documentSnapshot.toObject(Restaurant.class).getBookedUsersId()));
    }

    public void deleteNotBookedRestaurant() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot -> {
            restaurant.set(documentSnapshot.toObject(Restaurant.class));
            if (restaurant.get().getBookedUsersId().isEmpty())
                RestaurantHelper.deleteRestaurant(restaurantId);
        });
    }

    // --- GETTERS ---
    public ObservableField<Restaurant> getRestaurant() { return this.restaurant; }
}

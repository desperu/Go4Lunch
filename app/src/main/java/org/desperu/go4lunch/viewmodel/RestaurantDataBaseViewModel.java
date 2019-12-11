package org.desperu.go4lunch.viewmodel;

import androidx.databinding.ObservableField;

import org.desperu.go4lunch.api.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;

public class RestaurantDataBaseViewModel {

    private String restaurantId;
    private RestaurantDetailActivity restaurantDetailActivity;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();

    public RestaurantDataBaseViewModel(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public RestaurantDataBaseViewModel(RestaurantDetailActivity restaurantDetailActivity, String restaurantId) {
        this.restaurantDetailActivity = restaurantDetailActivity;
        this.restaurantId = restaurantId;
    }

    public void fetchRestaurant() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot ->
                restaurant.set(documentSnapshot.toObject(Restaurant.class)));
    }

    public void getRestaurantBookedUsers() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot -> {
            Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
            if (restaurant != null) {
                restaurantDetailActivity.setBookedUserId(restaurant.getBookedUsersId());
                restaurantDetailActivity.updateRecyclerView();
            }
        });
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

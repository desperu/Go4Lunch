package org.desperu.go4lunch.viewmodel;

import androidx.databinding.ObservableField;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.api.firestore.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;

public class RestaurantDBViewModel {

    private String restaurantId;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();

    public RestaurantDBViewModel(String restaurantId) {
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
     * Get restaurants booked users from restaurant detail activity.
     */
    public void getRestaurant(RestaurantDetailActivity activity) {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot ->
                activity.updateRecyclerView(documentSnapshot.toObject(Restaurant.class)));
    }

    /**
     * Get restaurants booked users from maps fragment.
     */
    public void getRestaurant(MapsFragment fragment, Place place) {
        RestaurantHelper.getRestaurant(restaurantId)
                .addOnSuccessListener(documentSnapshot ->
                fragment.addMarker(documentSnapshot.toObject(Restaurant.class), place))
                .addOnFailureListener(e -> fragment.addMarker(null, place));
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

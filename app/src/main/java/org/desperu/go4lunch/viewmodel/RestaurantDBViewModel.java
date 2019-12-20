package org.desperu.go4lunch.viewmodel;

import androidx.databinding.ObservableDouble;
import androidx.databinding.ObservableField;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.api.firestore.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;

import java.util.ArrayList;

public class RestaurantDBViewModel {

    private String restaurantId;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();
    private ObservableDouble likeUsersNumber = new ObservableDouble();

    public RestaurantDBViewModel(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Fetch restaurant data from firestore.
     */
    public void fetchRestaurant() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot -> {
            this.restaurant.set(documentSnapshot.toObject(Restaurant.class));
            if (restaurant.get() != null && restaurant.get().getUsersLike() != null)
                this.likeUsersNumber.set(restaurant.get().getUsersLike().size() / (double) 10);
        });
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

    /**
     * Update restaurant like users.
     * @param place Restaurant place object.
     * @param userId Id of user.
     */
    public void updateRestaurantUsersLike(Place place, String userId) {
        if (restaurant.get() == null)
            RestaurantHelper.createRestaurant(place.getId(), place.getName(), new ArrayList<>(),
                    place.getOpeningHours().toString(), place.getTypes().toString(), place.getRating(), new ArrayList<>());
        RestaurantHelper.updateLikeUsers(restaurantId, userId);
    }

    /**
     * Remove restaurant like user.
     * @param userId User id.
     */
    public void removeRestaurantUserLike(String userId) {
        RestaurantHelper.removeLikeUser(restaurantId, userId);
    }

    // --- GETTERS ---
    public ObservableField<Restaurant> getRestaurant() { return this.restaurant; }

    public ObservableDouble getLikeUsersNumber() { return this.likeUsersNumber; }
}

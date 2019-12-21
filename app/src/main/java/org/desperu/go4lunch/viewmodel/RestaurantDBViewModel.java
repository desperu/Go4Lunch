package org.desperu.go4lunch.viewmodel;

import androidx.databinding.ObservableDouble;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.api.firestore.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;

import java.util.ArrayList;

public class RestaurantDBViewModel {

    // FOR DATA
    private String restaurantId;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();
    private ObservableDouble likeUsersNumber = new ObservableDouble();
// TODO to clean if good
    private MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();

    // CONSTRUCTOR
    public RestaurantDBViewModel(String restaurantId) { this.restaurantId = restaurantId; }

    // --------------
    // REQUEST
    // --------------

    /**
     * Fetch restaurant data from firestore.
     */
    public void fetchRestaurant() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot -> {
            this.restaurant.set(documentSnapshot.toObject(Restaurant.class));
            this.restaurantLiveData.postValue(documentSnapshot.toObject(Restaurant.class)); // TODO use ObservableField or MutableLiveData
            if (restaurant.get() != null && restaurant.get().getLikeUsers() != null)
                this.likeUsersNumber.set(restaurant.get().getLikeUsers().size() / (double) 10);
        });
    }

    /**
     * Update restaurant like users.
     * @param place Restaurant place object.
     * @param userId Id of user.
     */
    public void updateRestaurantLikeUsers(Place place, String userId) {
        if (restaurant.get() == null)
            RestaurantHelper.createRestaurant(place.getId(), place.getName(), new ArrayList<>(),
                    place.getOpeningHours().toString(), place.getTypes().toString(), place.getRating(), new ArrayList<>());
        RestaurantHelper.updateLikeUsers(restaurantId, userId);
    }

    /**
     * Remove restaurant like user.
     * @param userId User id.
     */
    public void removeRestaurantLikeUser(String userId) {
        RestaurantHelper.removeLikeUser(restaurantId, userId);
    }

    // --- GETTERS ---
    public ObservableField<Restaurant> getRestaurant() { return this.restaurant; }

    public ObservableDouble getLikeUsersNumber() { return this.likeUsersNumber; }

    public LiveData<Restaurant> getRestaurantLiveData() { return this.restaurantLiveData; }
}

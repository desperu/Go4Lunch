package org.desperu.go4lunch.viewmodel;

import androidx.databinding.ObservableDouble;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.api.firestore.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class RestaurantDBViewModel {

    // FOR DATA
    private String restaurantId;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();
    private ObservableField<String> bookedUsersNumber = new ObservableField<>();
    private ObservableDouble likeUsersNumber = new ObservableDouble();
    private ObservableInt likeUsersNumberString = new ObservableInt();
    private MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();

    // CONSTRUCTOR
    public RestaurantDBViewModel(String restaurantId) { this.restaurantId = restaurantId; }

    // --------------
    // REQUEST
    // --------------

    /**
     * Create restaurant in firestore.
     * @param place Restaurant place object.
     */
    void createRestaurant(@NotNull Place place) {
        RestaurantHelper.createRestaurant(place.getId(), place.getName(), new ArrayList<>(), place.getRating(), new ArrayList<>());
    }

    /**
     * Fetch restaurant data from firestore.
     */
    public void fetchRestaurant() {
        RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot -> {
            this.restaurant.set(documentSnapshot.toObject(Restaurant.class));
            this.restaurantLiveData.postValue(documentSnapshot.toObject(Restaurant.class));
            this.bookedUsersNumber.set(Go4LunchUtils.getBookedUsersNumber(documentSnapshot.toObject(Restaurant.class)));
            if (restaurant.get() != null && Objects.requireNonNull(restaurant.get()).getLikeUsers() != null)
                this.likeUsersNumber.set(Objects.requireNonNull(restaurant.get()).getLikeUsers().size() / (double) 10);
        });
    }

    /**
     * Update restaurant booked user in firestore.
     * @param userId User Id.
     */
    void updateRestaurantBookedUser(String userId) {
        RestaurantHelper.updateBookedUsers(this.restaurantId, userId);
    }

    /**
     * Remove restaurant booked user in firestore.
     * @param restaurantId Restaurant Id.
     * @param userId User Id.
     */
    void removeRestaurantBookedUser(String restaurantId, String userId) {
        RestaurantHelper.removeBookedUser(restaurantId, userId);
    }
    /**
     * Update restaurant like users.
     * @param place Restaurant place object.
     * @param userId Id of user.
     */
    public void updateRestaurantLikeUsers(Place place, String userId) {
        if (restaurant.get() == null) this.createRestaurant(place);
        RestaurantHelper.updateLikeUsers(this.restaurantId, userId);
    }

    /**
     * Remove restaurant like user.
     * @param restaurantId Restaurant Id.
     * @param userId User id.
     */
    public void removeRestaurantLikeUser(String restaurantId, String userId) {
        RestaurantHelper.removeLikeUser(restaurantId, userId);
    }

    // --- GETTERS ---
    public ObservableField<Restaurant> getRestaurant() { return this.restaurant; }

    public ObservableField<String> getBookedUsersNumber() { return this.bookedUsersNumber; }

    public ObservableInt getLikeUsersNumberString(double placeRating, int starPosition) { // TODO BindingAdapter
        this.likeUsersNumberString.set(Go4LunchUtils.getRatingStarState(this.likeUsersNumber.get(), placeRating, starPosition));
        return this.likeUsersNumberString;
    }

    public LiveData<Restaurant> getRestaurantLiveData() { return this.restaurantLiveData; }
}
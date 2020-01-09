package org.desperu.go4lunch.viewmodel;

import android.app.Application;

import androidx.databinding.ObservableDouble;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.api.firestore.RestaurantHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RestaurantDBViewModel extends AndroidViewModel {

    // FOR DATA
    private String restaurantId;
    private ObservableField<Restaurant> restaurant = new ObservableField<>();
    private ObservableField<String> bookedUsersNumber = new ObservableField<>();
    private ObservableDouble likeUsersNumber = new ObservableDouble();
    private ObservableInt starOneState = new ObservableInt();
    private ObservableInt starTwoState = new ObservableInt();
    private ObservableInt starThreeState = new ObservableInt();
    private MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();

    // CONSTRUCTOR
    public RestaurantDBViewModel(Application application, String restaurantId) {
        super(application);
        this.restaurantId = restaurantId;
        this.setStarsStates(0); // Hide stars before fetch data.
    }

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
            this.setRestaurantData(documentSnapshot.toObject(Restaurant.class));
            this.fetchRestaurantInfoRating();
        });
    }

    /**
     * Fetch restaurant info rating, from google place.
     */
    private void fetchRestaurantInfoRating() {
        RestaurantInfoViewModel restaurantInfoViewModel = new RestaurantInfoViewModel(getApplication(), restaurantId);
        restaurantInfoViewModel.getPlaceLiveData().observeForever(place ->
                this.setStarsStates(place.getRating() != null ? place.getRating() : 0));
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

    // --- SETTERS ---

    /**
     * Set restaurant data when received response.
     * @param restaurant Received restaurant object.
     */
    private void setRestaurantData(Restaurant restaurant) {
        this.restaurant.set(restaurant);
        this.restaurantLiveData.postValue(restaurant);
        this.bookedUsersNumber.set(Go4LunchUtils.getBookedUsersNumber(restaurant));
        if (restaurant != null && restaurant.getLikeUsers() != null)
            this.likeUsersNumber.set(restaurant.getLikeUsers().size() / (double) 10);
    }

    /**
     * Set Stars states.
     * @param placeRating Restaurant rating from google place.
     */
    private void setStarsStates(double placeRating) {
        this.starOneState.set(Go4LunchUtils.getRatingStarState(this.likeUsersNumber.get(), placeRating, 1));
        this.starTwoState.set(Go4LunchUtils.getRatingStarState(this.likeUsersNumber.get(), placeRating, 2));
        this.starThreeState.set(Go4LunchUtils.getRatingStarState(this.likeUsersNumber.get(), placeRating, 3));
    }

    // For live data test only
    void setRestaurantLiveData(Restaurant restaurant) { this.restaurantLiveData.setValue(restaurant); }

    // --- GETTERS ---
    public ObservableField<Restaurant> getRestaurant() { return this.restaurant; }

    public ObservableField<String> getBookedUsersNumber() { return this.bookedUsersNumber; }

    public ObservableInt getStarOneState() { return this.starOneState; }

    public ObservableInt getStarTwoState() { return this.starTwoState; }

    public ObservableInt getStarThreeState() { return this.starThreeState; }

    public LiveData<Restaurant> getRestaurantLiveData() { return this.restaurantLiveData; }
}
package org.desperu.go4lunch.viewmodel;

import android.app.Application;
import android.graphics.Typeface;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.firestore.UserHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.utils.Go4LunchPrefs;
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static org.desperu.go4lunch.Go4LunchTools.CodeResponse.*;
import static org.desperu.go4lunch.Go4LunchTools.PrefsKeys.*;
import static org.desperu.go4lunch.Go4LunchTools.SettingsDefault.BOOKED_NOTIFICATION_SENT_DEFAULT;

public class UserDBViewModel extends AndroidViewModel {

    private String uid;
    private ObservableField<User> user = new ObservableField<>();
    private ObservableField<String> joiningName = new ObservableField<>();
    private ObservableField<String> userEating = new ObservableField<>();
    private ObservableBoolean userEatingDecided = new ObservableBoolean();
    private MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private MutableLiveData<List<User>> allUsersList = new MutableLiveData<>();
    private MutableLiveData<Integer> updateBookedResponse = new MutableLiveData<>();

    // CONSTRUCTORS
    public UserDBViewModel(Application application) { super(application); }

    public UserDBViewModel(Application application, String uid) {
        super(application);
        this.uid = uid;
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Create user in firestore.
     */
    public void createUserInFirestore(String uid, String userName, String urlPicture) {
        UserHelper.createUser(uid, userName, urlPicture);
    }

    /**
     * Fetch user from firestore.
     */
    public void fetchUser() {
        UserHelper.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            User user = documentSnapshot.toObject(User.class);
            user = user != null ? user : new User();
            this.setUserData(user);
            this.fetchBookedRestaurant(user);
        }).addOnFailureListener(e -> setUserData(new User()));
    }

    /**
     * Fetch user booked restaurant from firestore.
     */
    private void fetchBookedRestaurant(@NotNull User user) {
        if (user.getBookedRestaurantId() != null && !user.getBookedRestaurantId().isEmpty()) {
            RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(
                    getApplication(), user.getBookedRestaurantId());
            restaurantDBViewModel.fetchRestaurant();
            restaurantDBViewModel.getRestaurantLiveData().observeForever(restaurant ->
                    this.setUserEating(user.getUserName(), restaurant.getName())
            );
        } else this.setUserEating(user.getUserName(), null);
    }

    /**
     * Fetch all users from firestore.
     */
    public void fetchAllUsers() {
        UserHelper.getAllUsers().addOnSuccessListener(queryDocumentSnapshots ->
                this.allUsersList.postValue(queryDocumentSnapshots.toObjects(User.class)));
    }

    /**
     * Update bookedRestaurant and bookedUser in firestore.
     * @param newBookedRestaurant New booked restaurant place object.
     * @param newBookedRestaurantDB New booked restaurant data base object.
     */
    public void updateBookedRestaurant(@NotNull Place newBookedRestaurant, Restaurant newBookedRestaurantDB) {
        RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(getApplication(), newBookedRestaurant.getId());

        UserHelper.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            // Get old booked restaurant before update.
            String oldBookedRestaurantId = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getBookedRestaurantId();

            if (oldBookedRestaurantId != null)
                // Remove user id from old booked restaurant list.
                restaurantDBViewModel.removeRestaurantBookedUser(oldBookedRestaurantId, uid);

            if (oldBookedRestaurantId != null && oldBookedRestaurantId.equals(newBookedRestaurant.getId())
                    || newBookedRestaurantDB != null && newBookedRestaurantDB.getName().equals(RESET_BOOKED_RESTAURANT)) {
                // If clicked when already booked, remove booked restaurant.
                UserHelper.updateBookedRestaurant(uid, null);
                updateBookedResponse.postValue(UNBOOKED);
            } else {
                // Update user's bookedRestaurantId in firestore.
                UserHelper.updateBookedRestaurant(uid, newBookedRestaurant.getId());

                // Update restaurant's bookedUsersId in firestore.
                if (newBookedRestaurantDB == null) restaurantDBViewModel.createRestaurant(newBookedRestaurant);
                restaurantDBViewModel.updateRestaurantBookedUser(uid);

                // Save booked time, and reset notification sent marker
                Go4LunchPrefs.savePref(getApplication(), BOOKED_RESTAURANT_TIME, System.currentTimeMillis());
                Go4LunchPrefs.savePref(getApplication(), IS_BOOKED_NOTIFICATION_SENT, BOOKED_NOTIFICATION_SENT_DEFAULT);

                // Handle action response
                updateBookedResponse.postValue(BOOKED);
            }
        }).addOnFailureListener(e -> updateBookedResponse.postValue(ERROR));
    }

    // --- SETTERS ---

    /**
     * Set user data when received response.
     * @param user Received user object.
     */
    private void setUserData(@NotNull User user) {
        this.user.set(user);
        this.userLiveData.postValue(user);
        this.joiningName.set(Go4LunchUtils.getJoiningName(getApplication(), user.getUserName()));
    }

    /**
     * Set user eating string and decided for string style.
     * @param userName User name.
     * @param restaurantName Booked restaurant name.
     */
    private void setUserEating(String userName, String restaurantName) {
        this.userEating.set(Go4LunchUtils.getUserEatingAt(getApplication(), userName, restaurantName));
        this.userEatingDecided.set(Go4LunchUtils.getUserDecided());
    }

    // For live data test only
    void setUserLiveData(User user) { this.userLiveData.setValue(user); }

    void setAllUsersList(List<User> allUsersList) { this.allUsersList.setValue(allUsersList);}

    void setUpdateBookedResponse(int bookedResponse) { this.updateBookedResponse.setValue(bookedResponse); }

    // --- GETTERS ---
    public ObservableField<User> getUser() { return this.user; }

    public ObservableField<String> getJoiningName() { return this.joiningName; }

    public ObservableField<String> getUserEating() { return this.userEating; }

    public ObservableBoolean getUserEatingDecided() { return userEatingDecided; }

    public LiveData<User> getUserLiveData() { return this.userLiveData; }

    public LiveData<List<User>> getAllUsersListLiveData() { return this.allUsersList; }

    public LiveData<Integer> getUpdateBookedResponse() { return this.updateBookedResponse; }

    @BindingAdapter("pictureUrl")
    public static void setImageUrl(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url != null && !url.isEmpty() ? url : R.drawable.ic_anon_user_48dp)
                .circleCrop()
                .into(imageView);
    }

    @BindingAdapter("userEatingStyle")
    public static void setUserEatingStyle(@NotNull TextView textView, boolean userEatingDecided) {
        textView.setTextColor(textView.getContext().getResources().getColor(
                userEatingDecided ? R.color.colorDark : R.color.colorLightGrey));
        textView.setTypeface(null, userEatingDecided ? Typeface.NORMAL : Typeface.ITALIC);
    }
}
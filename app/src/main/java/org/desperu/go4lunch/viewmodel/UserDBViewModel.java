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
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static org.desperu.go4lunch.Go4LunchTools.CodeResponse.*;

public class UserDBViewModel extends AndroidViewModel {

    private String uid;
    private ObservableField<User> user = new ObservableField<>();
    private ObservableField<String> userName = new ObservableField<>();
    private ObservableField<String> userEating = new ObservableField<>();
    private ObservableField<String> joiningName = new ObservableField<>();
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
            this.user.set(documentSnapshot.toObject(User.class));
            this.userLiveData.postValue(documentSnapshot.toObject(User.class));
            if (user.get() != null && Objects.requireNonNull(user.get()).getUserName() != null) {
                userName.set(Objects.requireNonNull(this.user.get()).getUserName());
                this.joiningName.set(Go4LunchUtils.getJoiningName(getApplication(),
                        Objects.requireNonNull(user.get()).getUserName()));
            }
        });
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
        RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(newBookedRestaurant.getId());

        UserHelper.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            // Get old booked restaurant before update.
            String oldBookedRestaurant = Objects.requireNonNull(documentSnapshot.toObject(User.class)).getBookedRestaurantId();

            if (oldBookedRestaurant != null)
                // Remove user id from old booked restaurant.
                restaurantDBViewModel.removeRestaurantBookedUser(oldBookedRestaurant, uid);

            if (oldBookedRestaurant != null && oldBookedRestaurant.equals(newBookedRestaurant.getId())) {
                // If clicked when already booked, remove booked restaurant.
                UserHelper.updateBookedRestaurant(uid, null); // TODO modify floating button
                updateBookedResponse.postValue(UNBOOKED);
            } else {
                // Update user's bookedRestaurantId in firestore.
                UserHelper.updateBookedRestaurant(uid, newBookedRestaurant.getId());

                // Update restaurant's bookedUsersId in firestore.
                if (newBookedRestaurantDB == null) restaurantDBViewModel.createRestaurant(newBookedRestaurant);

                restaurantDBViewModel.updateRestaurantBookedUser(uid);
                updateBookedResponse.postValue(BOOKED);
            }
        }).addOnFailureListener(e -> updateBookedResponse.postValue(ERROR));
    }

    // --- GETTERS ---
    public ObservableField<User> getUser() { return this.user; }

    public ObservableField<String> getJoiningName() { return this.joiningName; }

    public ObservableBoolean getUserEatingDecided() { return userEatingDecided; }

    public LiveData<User> getUserLiveData() { return this.userLiveData; }

    public LiveData<List<User>> getAllUsersListLiveData() { return this.allUsersList; }

    public LiveData<Integer> getUpdateBookedResponse() { return this.updateBookedResponse; }

    @BindingAdapter("pictureUrl")
    public static void setImageUrl(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url).circleCrop().into(imageView);
    }

    public ObservableField<String> getUserEating(String restaurantName) {
        this.userEating.set(Go4LunchUtils.getUserEatingAt(getApplication(), this.userName.get(), restaurantName));
        this.userEatingDecided.set(Go4LunchUtils.getUserDecided());
        return this.userEating;
    }

//    @BindingAdapter("userEatingAt")
//    public static void setUserEatingAt(@NotNull TextView textView, String restaurantName) {
//        textView.setText(Go4LunchUtils.getUserEatingAt(textView.getContext(), userName.get(), restaurantName));
//        userEatingDecided.set(Go4LunchUtils.getUserDecided());
//    }

    @BindingAdapter("userEatingStyle")
    public static void setUserEatingStyle(@NotNull TextView textView, boolean userEatingDecided) {
        textView.setTextColor(textView.getContext().getResources().getColor(
                userEatingDecided ? R.color.colorDark : R.color.colorLightGrey));
        textView.setTypeface(null, userEatingDecided ? Typeface.NORMAL : Typeface.ITALIC);
    }
}
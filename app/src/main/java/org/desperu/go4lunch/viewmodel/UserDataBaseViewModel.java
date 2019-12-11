package org.desperu.go4lunch.viewmodel;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.api.RestaurantHelper;
import org.desperu.go4lunch.api.UserHelper;
import org.desperu.go4lunch.models.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class UserDataBaseViewModel {

    private String uid;
    private ObservableField<User> user = new ObservableField<>();

    public UserDataBaseViewModel(String uid) {
        this.uid = uid;
    }

    /**
     * Fetch user from firestore.
     */
    public void fetchUser() {
        UserHelper.getUser(uid).addOnSuccessListener(documentSnapshot ->
                user.set(documentSnapshot.toObject(User.class)));
    }

    /**
     * Update bookedRestaurant and bookedUser in firestore.
     * @param newBookedRestaurant New booked restaurant id.
     */
    public void updateBookedRestaurant(Place newBookedRestaurant) {
        UserHelper.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            // Get old booked restaurant before update.
            String olBookedRestaurant = documentSnapshot.toObject(User.class).getBookedRestaurantId();

            if (olBookedRestaurant != null)
                // Remove user id from old booked restaurant.
                RestaurantHelper.removeBookedUser(olBookedRestaurant, uid);

            if (olBookedRestaurant != null && olBookedRestaurant.equals(newBookedRestaurant.getId()))
                UserHelper.updateBookedRestaurant(uid, null); // TODO modify floating button
            else {
                // Update user's bookedRestaurantId in firestore.
                UserHelper.updateBookedRestaurant(uid, newBookedRestaurant.getId());

                // Update restaurant's bookedUsersId in firestore.
                List<String> userList = new ArrayList<>();

                RestaurantHelper.createRestaurant(newBookedRestaurant.getId(), newBookedRestaurant.getName(), userList,
                        newBookedRestaurant.getOpeningHours().toString(), newBookedRestaurant.getTypes().toString(), // TODO can be null object...
                        newBookedRestaurant.getRating());
                RestaurantHelper.updateBookedUsers(newBookedRestaurant.getId(), uid);
            }
        });
    }

    // --- GETTERS ---
    public ObservableField<User> getUser() { return this.user; }

    public String getPictureUrl() { return this.user.get().getUrlPicture(); }

    @BindingAdapter("pictureUrl") public static void setImageUrl(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url).circleCrop().into(imageView);
    }
}
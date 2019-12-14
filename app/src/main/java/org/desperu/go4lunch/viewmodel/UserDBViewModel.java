package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.RestaurantHelper;
import org.desperu.go4lunch.api.UserHelper;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.view.main.fragments.WorkmatesFragment;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static org.desperu.go4lunch.Go4LunchTools.CodeResponse.*;

public class UserDBViewModel {

    private Context context;
    private String uid;
    private ObservableField<User> user = new ObservableField<>();

    private ObservableField<String> pictureUrl = new ObservableField<>();
    private ObservableField<String> userName = new ObservableField<>();
    private ObservableField<String> joiningName = new ObservableField<>();

    public UserDBViewModel() { }

    public UserDBViewModel(Context context, String uid) {
        this.context = context;
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
            user.set(documentSnapshot.toObject(User.class));
            pictureUrl.set(user.get().getUrlPicture()); // TODO useless?
            userName.set(user.get().getUsername()); // TODO useless?
            joiningName.set(context.getResources().getString(R.string.activity_restaurant_detail_recycler_text_joining, this.userName.get()));
        });
    }

    /**
     * Fetch all users from firestore.
     */
    public void fetchAllUsers(WorkmatesFragment fragment) {
        UserHelper.getAllUsers().addOnSuccessListener(queryDocumentSnapshots ->
                fragment.updateRecyclerView(queryDocumentSnapshots.toObjects(User.class)));
    }

    /**
     * Update bookedRestaurant and bookedUser in firestore.
     * @param newBookedRestaurant New booked restaurant id.
     * @param activity Instance of RestaurantDetailActivity.
     */
    public void updateBookedRestaurant(@NotNull RestaurantDetailActivity activity, Place newBookedRestaurant) {
        UserHelper.getUser(uid).addOnSuccessListener(documentSnapshot -> {
            // Get old booked restaurant before update.
            String olBookedRestaurant = documentSnapshot.toObject(User.class).getBookedRestaurantId();

            if (olBookedRestaurant != null)
                // Remove user id from old booked restaurant.
                RestaurantHelper.removeBookedUser(olBookedRestaurant, uid);

            if (olBookedRestaurant != null && olBookedRestaurant.equals(newBookedRestaurant.getId())) {
                // If clicked when already booked, remove booked restaurant.
                UserHelper.updateBookedRestaurant(uid, null); // TODO modify floating button
                activity.handleResponseAfterBooking(UNBOOKED);
            } else {
                // Update user's bookedRestaurantId in firestore.
                UserHelper.updateBookedRestaurant(uid, newBookedRestaurant.getId());

                // Update restaurant's bookedUsersId in firestore.
                List<String> userList = new ArrayList<>();

                RestaurantHelper.createRestaurant(newBookedRestaurant.getId(), newBookedRestaurant.getName(), userList,
                        newBookedRestaurant.getOpeningHours().toString(), newBookedRestaurant.getTypes().toString(), // TODO can be null object...
                        newBookedRestaurant.getRating());
                RestaurantHelper.updateBookedUsers(newBookedRestaurant.getId(), uid);
                activity.handleResponseAfterBooking(BOOKED);
            }
        }).addOnFailureListener(e -> activity.handleResponseAfterBooking(ERROR));
    }

    // --- GETTERS ---
    public ObservableField<User> getUser() { return this.user; }

    public ObservableField<String> getJoiningName() { return this.joiningName; }

    @BindingAdapter("pictureUrl") public static void setImageUrl(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url).circleCrop().into(imageView);
    }
}
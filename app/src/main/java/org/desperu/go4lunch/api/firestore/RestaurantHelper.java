package org.desperu.go4lunch.api.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.desperu.go4lunch.models.Restaurant;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RestaurantHelper {

    private static final String COLLECTION_NAME = "restaurants";
    private static final String FIELD_BOOKED_USERS = "bookedUsersId";
    private static final String FIELD_LIKE_USERS = "likeUsers";

    // --- COLLECTION REFERENCE ---

    @NotNull
    private static CollectionReference getRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    @NotNull
    public static Task<Void> createRestaurant(String restaurantId, String name, List<String> bookedUsersId,
                                              Double stars, List<String> likeUsers) {
        Restaurant restaurantToCreate = new Restaurant(name, restaurantId, bookedUsersId, stars, likeUsers);
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).set(restaurantToCreate);
    }

    // --- GET ---

    @NotNull
    public static Task<DocumentSnapshot> getRestaurant(String restaurantId) {
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).get();
    }

    // --- UPDATE ---

    @NotNull
    public static Task<Void> updateBookedUsers(String restaurantId, String usersId) {
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).update(FIELD_BOOKED_USERS, FieldValue.arrayUnion(usersId));
    }

    @NotNull
    public static Task<Void> updateLikeUsers(String restaurantId, String userId) {
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).update(FIELD_LIKE_USERS, FieldValue.arrayUnion(userId));
    }

    // --- REMOVE ---

    @NotNull
    public static Task<Void> removeBookedUser(String restaurantId, String userId) {
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).update(FIELD_BOOKED_USERS, FieldValue.arrayRemove(userId));
    }

    @NotNull
    public static Task<Void> removeLikeUser(String restaurantId, String userId) {
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).update(FIELD_LIKE_USERS, FieldValue.arrayRemove(userId));
    }

    // --- DELETE ---

    @NotNull
    public static Task<Void> deleteRestaurant(String restaurantId) {
        return RestaurantHelper.getRestaurantCollection().document(restaurantId).delete();
    }
}

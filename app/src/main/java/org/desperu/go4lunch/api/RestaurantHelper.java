package org.desperu.go4lunch.api;

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

    // --- COLLECTION REFERENCE ---

    @NotNull
    private static CollectionReference getRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    @NotNull
    public static Task<Void> createRestaurant(String id, String name, List<String> bookedUserId, String openHours, String restaurantType, Double stars) {
        Restaurant restaurantToCreate = new Restaurant(name, id, bookedUserId, openHours,restaurantType, stars);
        return RestaurantHelper.getRestaurantCollection().document(id).set(restaurantToCreate);
    }

    // --- GET ---

    @NotNull
    public static Task<DocumentSnapshot> getRestaurant(String id) {
        return RestaurantHelper.getRestaurantCollection().document(id).get();
    }

    // --- UPDATE ---

    @NotNull
    public static Task<Void> updateBookedUsers(String id, String usersId) {
        return RestaurantHelper.getRestaurantCollection().document(id).update("bookedUsersId", FieldValue.arrayUnion(usersId));
    }

    // --- REMOVE ---

    @NotNull
    public static Task<Void> removeBookedUser(String id, String userId) {
        return RestaurantHelper.getRestaurantCollection().document(id).update("bookedUsersId", FieldValue.arrayRemove(userId));
    }

    // --- DELETE ---

    @NotNull
    public static Task<Void> deleteRestaurant(String id) {
        return RestaurantHelper.getRestaurantCollection().document(id).delete();
    }
}

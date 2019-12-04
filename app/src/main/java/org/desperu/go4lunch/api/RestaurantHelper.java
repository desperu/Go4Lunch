package org.desperu.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.desperu.go4lunch.models.Restaurant;
import org.jetbrains.annotations.NotNull;

public class RestaurantHelper {

    private static final String COLLECTION_NAME = "restaurant";

    // --- COLLECTION REFERENCE ---

    @NotNull
    private static CollectionReference getRestaurantCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    @NotNull
    public static Task<Void> createRestaurant(String id, String name, String bookedUser, String openHours, String restaurantType, String stars) {
        Restaurant restaurantToCreate = new Restaurant(name, id, bookedUser, openHours,restaurantType, stars);
        return RestaurantHelper.getRestaurantCollection().document(id).set(restaurantToCreate);
    }

    // --- GET ---

    @NotNull
    public static Task<DocumentSnapshot> getRestaurant(String id){
        return RestaurantHelper.getRestaurantCollection().document(id).get();
    }

    // --- UPDATE ---

    public static Task<Void> updateBookedUsers(String id, String[] usersNames) {
        return RestaurantHelper.getRestaurantCollection().document(id).update("bookedUsers", usersNames);
    }

    // --- DELETE ---

    @NotNull
    public static Task<Void> deleteRestaurant(String id) {
        return RestaurantHelper.getRestaurantCollection().document(id).delete();
    }
}

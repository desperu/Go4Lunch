package org.desperu.go4lunch.api;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.desperu.go4lunch.models.User;
import org.jetbrains.annotations.NotNull;

public class UserHelper {

    private static final String COLLECTION_NAME = "users";
    private static final String COLLECTION_USERNAME = "username";
    private static final String COLLECTION_RESTAURANT_ID = "bookedRestaurantId";

    // --- COLLECTION REFERENCE ---

    @NotNull
    private static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    @NotNull
    public static Task<Void> createUser(String uid, String username, String urlPicture) {
        User userToCreate = new User(uid, username, urlPicture, null);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    @NotNull
    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    // --- UPDATE ---

    @NotNull
    public static Task<Void> updateUsername(String username, String uid) {
        return UserHelper.getUsersCollection().document(uid).update(COLLECTION_USERNAME, username);
    }

    @NotNull
    public static Task<Void> updateBookedRestaurant(String uid, String bookedRestaurantId) {
        return UserHelper.getUsersCollection().document(uid).update(COLLECTION_RESTAURANT_ID, bookedRestaurantId);
    }

    // --- DELETE ---

    @NotNull
    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
}
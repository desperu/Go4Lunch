package org.desperu.go4lunch.api.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.desperu.go4lunch.models.User;
import org.jetbrains.annotations.NotNull;

public class UserHelper {

    private static final String COLLECTION_NAME = "users";
    private static final String FIELD_USERNAME = "userName";
    private static final String FILED_RESTAURANT_ID = "bookedRestaurantId";

    // --- COLLECTION REFERENCE ---

    @NotNull
    private static CollectionReference getUsersCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    // --- CREATE ---

    @NotNull
    public static Task<Void> createUser(String uid, String userName, String urlPicture) {
        User userToCreate = new User(uid, userName, urlPicture, null);
        return UserHelper.getUsersCollection().document(uid).set(userToCreate);
    }

    // --- GET ---

    @NotNull
    public static Task<DocumentSnapshot> getUser(String uid){
        return UserHelper.getUsersCollection().document(uid).get();
    }

    @NotNull
    public static Task<QuerySnapshot> getAllUsers() {
        return UserHelper.getUsersCollection().get();
    }

    // --- UPDATE ---

    @NotNull
    public static Task<Void> updateUsername(String userName, String uid) {
        return UserHelper.getUsersCollection().document(uid).update(FIELD_USERNAME, userName);
    }

    @NotNull
    public static Task<Void> updateBookedRestaurant(String uid, String bookedRestaurantId) {
        return UserHelper.getUsersCollection().document(uid).update(FILED_RESTAURANT_ID, bookedRestaurantId);
    }

    // --- DELETE ---

    @NotNull
    public static Task<Void> deleteUser(String uid) {
        return UserHelper.getUsersCollection().document(uid).delete();
    }
}
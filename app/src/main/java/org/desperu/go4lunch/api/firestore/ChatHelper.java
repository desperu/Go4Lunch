package org.desperu.go4lunch.api.firestore;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class ChatHelper {

    private static final String COLLECTION_NAME = "chat";

    // --- COLLECTION REFERENCE ---

    @NotNull
    public static CollectionReference getChatCollection(){
        return FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }
}
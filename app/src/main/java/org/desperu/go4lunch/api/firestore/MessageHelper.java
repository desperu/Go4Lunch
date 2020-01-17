package org.desperu.go4lunch.api.firestore;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import org.desperu.go4lunch.models.Message;
import org.desperu.go4lunch.models.User;
import org.jetbrains.annotations.NotNull;

public class MessageHelper {

    // --- GET ---

    /**
     * Get all messages for chat from firestore.
     * @return Fifty last messages of chat.
     */
    @NotNull
    public static Query getAllMessageForChat(){
        return ChatHelper.getChatCollection()
                .orderBy("dateCreated")
                .limit(500);
    }

    // --- PUSH ---

    /**
     * Create message object and push on firestore.
     * @param textMessage Text message.
     * @param userSender User object who send message.
     * @return Store message on Firestore.
     */
    @NotNull
    public static Task<DocumentReference> createMessageForChat(String textMessage, User userSender){

        // Create the Message object
        Message message = new Message(textMessage, userSender);

        // Store Message to Firestore
        return ChatHelper.getChatCollection()
                .add(message);
    }

    /**
     * Create message object with image and push on firestore.
     * @param urlImage url image.
     * @param textMessage Text message.
     * @param userSender User object who send message.
     * @return Store message on Firestore.
     */
    @NotNull
    public static Task<DocumentReference> createMessageWithImageForChat(String urlImage, String textMessage, User userSender){

        // Creating Message with the URL image
        Message message = new Message(textMessage, urlImage, userSender);

        // Storing Message on Firestore
        return ChatHelper.getChatCollection()
                .add(message);
    }
}
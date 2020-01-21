package org.desperu.go4lunch.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.firestore.MessageHelper;
import org.desperu.go4lunch.models.Message;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessageViewModel extends AndroidViewModel {

    // FOR DATA
    private MutableLiveData<List<Message>> allMessageList = new MutableLiveData<>();
    private ObservableBoolean isCurrentUser = new ObservableBoolean();
    private ObservableField<String> textMessage = new ObservableField<>();
    private ObservableInt textAlignment = new ObservableInt();
    private ObservableInt messageTextState = new ObservableInt();
    private ObservableField<String> dateMessage = new ObservableField<>();
    private ObservableField<String> userSenderName = new ObservableField<>();
    private ObservableField<String> senderUrlPicture = new ObservableField<>();
    private ObservableField<String> messageUrlPicture = new ObservableField<>();
    private ObservableInt messageImageState = new ObservableInt();

    // CONSTRUCTORS
    public MessageViewModel(Application application) { super(application); }

    public MessageViewModel(Application application, Message message, String currentUserId) {
        super(application);
        this.setMessageData(message, currentUserId);
        this.setProfileUserSender(message);
        this.setMessageImageUrl(message);
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Create message for chat.
     * @param message Text message.
     * @param userSender User sender.
     */
    public void createMessage(String message, User userSender) {
        MessageHelper.createMessageForChat(message, userSender)
                .addOnFailureListener(this.onFailureListener());
    }

    /**
     * Upload a picture in Firebase and send a message.
     * @param uriImageSelected Uri of selected image from phone storage.
     * @param message Text message.
     * @param currentUser User object of current user.
     */
    public void uploadPhotoInFirebaseAndSendMessage(Uri uriImageSelected, String message, User currentUser) {
        String uuid = UUID.randomUUID().toString(); // GENERATE UNIQUE STRING
        // UPLOAD TO GCS
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        mImageRef.putFile(uriImageSelected)
                .addOnSuccessListener(taskSnapshot -> {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(uri -> {
                        // SAVE MESSAGE IN FIRESTORE
                        MessageHelper.createMessageWithImageForChat(
                                uri.toString(), message, currentUser)
                                .addOnFailureListener(onFailureListener());
                    });
                })
                .addOnFailureListener(this.onFailureListener());
    }

    /**
     * Get all message for chat.
     * @return All messages.
     */
    public Query getAllMessage() {
        return MessageHelper.getAllMessageForChat();
    }

    /**
     * Get all message list for chat.
     */
    public void getAllMessageList() {
        MessageHelper.getAllMessageForChat().get().addOnSuccessListener(queryDocumentSnapshots ->
                allMessageList.postValue(queryDocumentSnapshots.toObjects(Message.class)));
    }

    // --------------------
    // ERROR HANDLER
    // --------------------

    @NotNull
    @Contract(pure = true)
    private OnFailureListener onFailureListener(){
        return e -> Toast.makeText(getApplication(), getApplication().getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
    }

    // --------------------
    // UTILS
    // --------------------

    /**
     * Get default margin, dp to pixel.
     * @param context Context from this method is called.
     * @return Pixel value.
     */
    private static int getDefaultMargin(@NotNull Context context) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        context.getResources().getDimension(R.dimen.default_margin),
                        context.getResources().getDisplayMetrics()) / 2.5);
    }

    /**
     * Get margin bottom message container, dp to pixel.
     * @param context Context from this method is called.
     * @return Pixel value.
     */
    private static int getMarginBottom(@NotNull Context context) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                context.getResources().getDimension(R.dimen.fragment_chat_message_item_container_text_and_image_message_container_margin_bottom),
                context.getResources().getDisplayMetrics()) / 2.5);
    }

    // --- SETTERS ---

    /**
     * Set message data.
     * @param message Message object.
     * @param currentUserId Current user Id.
     */
    private void setMessageData(@NotNull Message message, String currentUserId) {
        // Check if current user is the sender
        this.isCurrentUser.set(message.getUserSender().getUid().equals(currentUserId));

        // Set text and alignment message
        this.textMessage.set(message.getMessage());
        this.textAlignment.set(isCurrentUser.get() ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);
        // Set message visibility
        this.messageTextState.set(message.getMessage() == null ? View.GONE : View.VISIBLE);

        // Set date message
        if (message.getDateCreated() != null)
            this.dateMessage.set(Go4LunchUtils.convertDateToString(message.getDateCreated(), new Date()));
    }

    /**
     * Set profile user sender data.
     * @param message Message object.
     */
    private void setProfileUserSender(@NotNull Message message) {
        // Set profile sender urlPicture
        this.senderUrlPicture.set(message.getUserSender().getUrlPicture());

        // Set user sender name
        this.userSenderName.set(message.getUserSender().getUserName());
    }

    /**
     * Set message image data.
     * @param message Message object.
     */
    private void setMessageImageUrl(@NotNull Message message) {
        // Set message image sent url
        this.messageUrlPicture.set(message.getUrlImage());
        this.messageImageState.set(message.getUrlImage() != null ? View.VISIBLE : View.GONE);
    }

    // For live data test only
    void setAllMessageListLiveData(List<Message> allMessageList) { this.allMessageList.postValue(allMessageList); }

    // --- GETTERS ---
    public LiveData<List<Message>> getAllMessageListLiveData() { return this.allMessageList; }

    public ObservableBoolean getIsCurrentUser() { return isCurrentUser; }

    public ObservableField<String> getTextMessage() { return this.textMessage; }

    public ObservableInt getTextAlignment() { return this.textAlignment; }

    public ObservableInt getMessageTextState() { return this.messageTextState; }

    public ObservableField<String> getDateMessage() { return this.dateMessage; }

    public ObservableField<String> getUserSenderName() { return this.userSenderName; }

    public ObservableField<String> getSenderUrlPicture() { return this.senderUrlPicture; }

    public ObservableField<String> getMessageUrlPicture() { return this.messageUrlPicture; }

    public ObservableInt getMessageImageState() { return this.messageImageState; }

    @BindingAdapter("messageBackground")
    public static void setMessageContainerBackground(@NotNull LinearLayout linear, boolean isCurrentUser) {
        linear.setBackground(isCurrentUser ?
                linear.getContext().getResources().getDrawable(R.drawable.ic_chat_message_not_sender_background) :
                linear.getContext().getResources().getDrawable(R.drawable.ic_chat_message_sender_background));
    }

    @BindingAdapter("senderPicture")
    public static void setProfileImage(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url != null && !url.isEmpty() ?
                url : R.drawable.ic_anon_user_48dp).circleCrop().into(imageView);
    }

    @BindingAdapter("messagePicture")
    public static void setMessageImage(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url).into(imageView);
    }

    @BindingAdapter("linearAlignParent")
    public static void setProfileContainerAlignment(@NotNull LinearLayout linearLayout, boolean isCurrentUser) {
        RelativeLayout.LayoutParams paramsLayoutHeader = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutHeader.addRule(isCurrentUser ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START);
        linearLayout.setLayoutParams(paramsLayoutHeader);
    }

    @BindingAdapter("relativeAlignOf")
    public static void setMessageContainerAlignment(@NotNull RelativeLayout relativeLayout, boolean isCurrentUser) {
        RelativeLayout.LayoutParams paramsLayoutContent = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutContent.addRule(isCurrentUser ? RelativeLayout.START_OF : RelativeLayout.END_OF,
                R.id.fragment_chat_message_item_profile_container);
        paramsLayoutContent.addRule(RelativeLayout.ALIGN_PARENT_TOP, R.id.fragment_chat_message_item_root_view);
        relativeLayout.setLayoutParams(paramsLayoutContent);
    }

    @BindingAdapter("linearAlignment")
    public static void setTextMessageContainerAlignment(@NotNull LinearLayout linearLayout, boolean isCurrentUser) {
        RelativeLayout.LayoutParams paramsLayoutMessage = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsLayoutMessage.addRule(isCurrentUser ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START,
                R.id.fragment_chat_message_item_message_container);
        paramsLayoutMessage.addRule(RelativeLayout.BELOW, R.id.fragment_chat_message_item_container_image_sent_cardview);
        paramsLayoutMessage.setMargins(getDefaultMargin(linearLayout.getContext()), 0,
                getDefaultMargin(linearLayout.getContext()), getMarginBottom(linearLayout.getContext()));
        linearLayout.setLayoutParams(paramsLayoutMessage);
    }

    @BindingAdapter("cardViewAlignTo")
    public static void setImageContainerAlignment(@NotNull CardView cardView, boolean isCurrentUser) {
        RelativeLayout.LayoutParams paramsImageView = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsImageView.addRule(isCurrentUser ? RelativeLayout.ALIGN_PARENT_END : RelativeLayout.ALIGN_PARENT_START,
                R.id.fragment_chat_message_item_message_container);
        paramsImageView.setMargins(getDefaultMargin(cardView.getContext()), 0,
                getDefaultMargin(cardView.getContext()), getMarginBottom(cardView.getContext()));
        cardView.setLayoutParams(paramsImageView);
    }

    @BindingAdapter("textAlignment")
    public static void setDateMessageTextAlignment(@NotNull TextView textView, boolean isCurrentUser) {
        textView.setTextAlignment(isCurrentUser ? View.TEXT_ALIGNMENT_TEXT_END : View.TEXT_ALIGNMENT_TEXT_START);
    }
}
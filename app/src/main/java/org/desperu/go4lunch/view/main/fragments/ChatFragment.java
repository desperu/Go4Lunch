package org.desperu.go4lunch.view.main.fragments;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.common.BaseChangeEventListener;
import com.firebase.ui.common.BaseObservableSnapshotArray;
import com.firebase.ui.common.ChangeEventType;
import com.firebase.ui.firestore.ClassSnapshotParser;
import com.firebase.ui.firestore.FirestoreArray;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.MetadataChanges;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.models.Message;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.view.adapter.ChatAdapter;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.viewmodel.MessageViewModel;
import org.desperu.go4lunch.viewmodel.UserAuthViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;
import static org.desperu.go4lunch.Go4LunchTools.ChatFragment.*;

public class ChatFragment extends BaseFragment implements BaseChangeEventListener {

    // FOR DESIGN
    // Getting all views needed
    @BindView(R.id.fragment_chat_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fragment_chat_text_view_recycler_view_empty) TextView textViewRecyclerViewEmpty;
    @BindView(R.id.fragment_chat_message_edit_text) TextInputEditText editTextMessage;
    @BindView(R.id.fragment_chat_image_chosen_preview) ImageView imageViewPreview;

    // FOR DATA
    // Declaring Adapter and data
    private ChatAdapter chatAdapter;
    private List<MessageViewModel> messageViewModelList = new ArrayList<>();
    private MessageViewModel messageViewModel;
    private BaseObservableSnapshotArray observableSnapshotArray;
    @Nullable
    private User modelCurrentUser;
    // Uri of image selected by user
    private Uri uriImageSelected;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_chat; }

    @Override
    protected void configureDesign() {
        this.configureRecyclerView();
        this.getCurrentUserFromFirestore();
        this.getAllMessageList();
    }


    public ChatFragment() {
        // Needed empty constructor
    }

    @NotNull
    @Contract(" -> new")
    public static ChatFragment newInstance() { return new ChatFragment(); }

    // --------------------
    // METHODS OVERRIDE
    // --------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 3-2 - Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 4-6 - Calling the appropriate method after activity result
        this.handleResponse(requestCode, resultCode, data);
    }

    @Override
    public void onChildChanged(@NonNull ChangeEventType type, @NonNull Object snapshot, int newIndex, int oldIndex) {
        getAllMessageList();
    }

    @Override
    public void onDataChanged() { this.getAllMessageList();}

    @Override
    public void onError(@NonNull Object o) {}

    @Override
    public void onPause() {
        super.onPause();
        observableSnapshotArray.removeAllListeners();
    }

    // --------------------
    // ACTIONS
    // --------------------

    @OnClick(R.id.fragment_chat_send_button)
    // Depending if an image is set, we'll send different Message to Firestore
    void onClickSendMessage() {
        if (modelCurrentUser != null) {
            if (!TextUtils.isEmpty(editTextMessage.getText())) {
                // Check if the ImageView is set
                if (this.imageViewPreview.getDrawable() == null) {
                    // SEND A TEXT MESSAGE
                    this.getMessageViewModel().createMessage(editTextMessage.getText().toString(),
                            modelCurrentUser);
                    this.editTextMessage.setText("");
                } else {
                    // SEND A IMAGE + TEXT IMAGE
                    this.getMessageViewModel().uploadPhotoInFirebaseAndSendMessage(
                            this.uriImageSelected, editTextMessage.getText().toString(), this.modelCurrentUser);
                    this.editTextMessage.setText("");
                    this.imageViewPreview.setImageDrawable(null);
                }
            } else if (this.imageViewPreview.getDrawable() != null) {
                // SEND AN IMAGE
                this.getMessageViewModel().uploadPhotoInFirebaseAndSendMessage(
                        this.uriImageSelected,null, this.modelCurrentUser);
                this.imageViewPreview.setImageDrawable(null);
            }
        }
    }

    @OnClick(R.id.fragment_chat_add_file_button)
    // Calling the appropriate method
    @AfterPermissionGranted(PERMS_STORAGE)
    void onClickAddFile() { this.chooseImageFromPhone(); }

    // --------------------
    // FILE MANAGEMENT
    // --------------------

    /**
     * Choose image from phone.
     */
    private void chooseImageFromPhone(){
        assert getActivity() != null;
        if (!EasyPermissions.hasPermissions(getActivity(), PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.fragment_chat_popup_title_permission_files_access), PERMS_STORAGE, PERMS);
            return;
        }
        // Launch an "Selection Image" Activity
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    /**
     * Handle activity response (after user has chosen or not a picture).
     * @param requestCode Code of request.
     * @param resultCode Result code of request.
     * @param data Intent request result data.
     */
    private void handleResponse(int requestCode, int resultCode, Intent data){
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) { //SUCCESS
                this.uriImageSelected = data.getData();
                Glide.with(this) //SHOWING PREVIEW OF IMAGE
                        .load(this.uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(this.imageViewPreview);
            } else {
                Toast.makeText(getContext(), getString(R.string.fragment_chat_toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // --------------------
    // REST REQUESTS
    // --------------------

    /**
     * Get current user id.
     * @return Current user id.
     */
    @NotNull
    private String getCurrentUserId() { return new UserAuthViewModel().getCurrentUser().getUid(); }

    /**
     * Get Current User from Firestore
     */
    private void getCurrentUserFromFirestore() {
        assert getActivity() != null;
        UserDBViewModel userDBViewModel = new UserDBViewModel(getActivity().getApplication(), getCurrentUserId());
        userDBViewModel.fetchUser();
        userDBViewModel.getUserLiveData().observe(this, user -> this.modelCurrentUser = user);
    }

    /**
     * Get Message view model instance, create if null.
     * @return Message view model instance.
     */
    private MessageViewModel getMessageViewModel() {
        assert getActivity() != null;
        return messageViewModel = messageViewModel != null ? messageViewModel : new MessageViewModel(getActivity().getApplication());
    }

    /**
     * Get all message list, observe when received response.
     */
    private void getAllMessageList() {
        getMessageViewModel().getAllMessageList();
        getMessageViewModel().getAllMessageListLiveData().observe(this, this::updateRecyclerView);
    }

    // --------------------
    // UI
    // --------------------

    /**
     * Configure RecyclerView.
     */
    private void configureRecyclerView() {
        // Configure Adapter & RecyclerView
        this.chatAdapter = new ChatAdapter(R.layout.fragment_chat_message_item, messageViewModelList);
        // Observe firebase snapshot message array list
        this.observeSnapshotArray();
        // Attach the adapter to the recyclerView to populate items
        recyclerView.setAdapter(this.chatAdapter);
        // Set layout manager to position the items
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Observe firebase snapshot message array list.
     */
    private void observeSnapshotArray() {
        observableSnapshotArray = new FirestoreArray<>(
                getMessageViewModel().getAllMessage(), MetadataChanges.EXCLUDE,
                new ClassSnapshotParser<>(Message.class));
        observableSnapshotArray.addChangeEventListener(this);
    }

    /**
     * Update recycler view when received data.
     * @param messageList Message List.
     */
    private void updateRecyclerView(@NotNull List<Message> messageList) {
        assert getActivity() != null;
        this.messageViewModelList.clear();
        for (Message message : messageList) {
            // Create a message view model for each message
            MessageViewModel messageViewModel = new MessageViewModel(getActivity().getApplication(), message, getCurrentUserId());
            this.messageViewModelList.add(messageViewModel);
        }
        this.chatAdapter.notifyDataSetChanged();
        // Show TextView in case RecyclerView is empty
        textViewRecyclerViewEmpty.setVisibility(this.chatAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        recyclerView.smoothScrollToPosition(chatAdapter.getItemCount()); // Scroll to bottom on new messages
        // Check that observe snapshot array is running, else launch it
        if (!observableSnapshotArray.isListening()) this.observeSnapshotArray();
    }
}
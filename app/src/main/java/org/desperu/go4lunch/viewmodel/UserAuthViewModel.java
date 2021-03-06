package org.desperu.go4lunch.viewmodel;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.desperu.go4lunch.R;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserAuthViewModel {

    private FirebaseUser currentUser;

    public UserAuthViewModel() {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    // --- GETTERS ---
    public FirebaseUser getCurrentUser() { return currentUser; }

    public String getUid() { return currentUser.getUid(); }

    public String getUserName() { return currentUser.getDisplayName(); }

    public String getUserMail() { return currentUser.getEmail(); }

    public String getUserPicture() {
        if (currentUser.getPhotoUrl() != null)
            return Objects.requireNonNull(currentUser.getPhotoUrl()).toString();
        else return "";
    }

    @BindingAdapter("imageUrl") public static void setImageUrl(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext())
                .load(url != null && !url.isEmpty() ? url : R.drawable.ic_anon_user_48dp)
                .circleCrop()
                .into(imageView);
    }

    public void userLogOut() { FirebaseAuth.getInstance().signOut(); }
}
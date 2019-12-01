package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class UserViewModel {

    private Context context;
    private FirebaseUser currentUser;

    public UserViewModel(@NonNull Context context) {
        this.context = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public String getUid() { return currentUser.getUid(); }

    public String getUserName() { return currentUser.getDisplayName(); }

    public String getUserMail() { return currentUser.getEmail(); }

    public String getUserPicture() { return Objects.requireNonNull(currentUser.getPhotoUrl()).toString(); }

    @BindingAdapter("imageUrl") public static void setImageUrl(@NotNull ImageView imageView, String url) {
        Glide.with(imageView.getContext()).load(url).circleCrop().into(imageView);
    }

    public void userLogOut() { FirebaseAuth.getInstance().signOut(); }
}
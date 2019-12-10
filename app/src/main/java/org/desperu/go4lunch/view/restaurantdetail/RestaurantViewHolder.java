package org.desperu.go4lunch.view.restaurantdetail;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.UserHelper;
import org.desperu.go4lunch.models.User;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    //FOR DESIGN
    @BindView(R.id.fragment_restaurant_detail_item_circular_image_view) ImageView userImage;
    @BindView(R.id.fragment_restaurant_detail_text_view) TextView textView;

    private User user;

    public RestaurantViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateWithUser(String userId, RequestManager glide, Context context) {
        UserHelper.getUser(userId).addOnSuccessListener(documentSnapshot -> {
            user = documentSnapshot.toObject(User.class);
            updateUserName();
            updateUserImage(glide);
        });
    }


    private void updateUserImage(@NotNull RequestManager glide) {
        glide.load(user.getUrlPicture()).circleCrop().into(userImage);
    }

    private void updateUserName() {
        textView.setText(user.getUsername());
    }
}

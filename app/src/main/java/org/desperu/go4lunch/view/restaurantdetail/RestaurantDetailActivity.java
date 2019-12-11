package org.desperu.go4lunch.view.restaurantdetail;

import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.snackbar.Snackbar;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityRestaurantDetailBinding;
import org.desperu.go4lunch.viewmodel.RestaurantDataBaseViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantViewModel;
import org.desperu.go4lunch.viewmodel.UserDataBaseViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RestaurantDetailActivity extends BaseActivity {

    //FOR DESIGN
    @BindView(R.id.activity_restaurant_detail_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.activity_restaurant_detail_swipe_container) SwipeRefreshLayout swipeRefreshLayout;

    // FOR DATA
    public static final String RESTAURANT_ID = "restaurant id";

    private RestaurantViewModel restaurantViewModel;
    private RestaurantAdapter adapter;
    private List<String> bookedUserId;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getActivityLayout() { return R.layout.activity_restaurant_detail; }

    @Override
    protected void configureDesign() {
        this.configureDataBinding();
        this.configureSwipeRefreshLayout();
        this.drawBelowStatusBar();
    }

    // --------------
    // METHODS OVERRIDE
    // --------------

    @Override
    protected void onResume() {
        super.onResume();
        this.getRestaurantBookedUsers();
    }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * For design, draw below status bar.
     */
    private void drawBelowStatusBar() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Get restaurant id from intent data.
     * @return The restaurant id.
     */
    private String getIdFromIntentData() { return getIntent().getStringExtra(RESTAURANT_ID); }

    /**
     * Get restaurant's bookedUsers from firestore.
     */
    private void getRestaurantBookedUsers() {
        RestaurantDataBaseViewModel restaurantDataBaseViewModel =
                new RestaurantDataBaseViewModel(this, this.getIdFromIntentData());
        restaurantDataBaseViewModel.getRestaurantBookedUsers();
    }

    /**
     * Configure data binding.
     */
    private void configureDataBinding() {
        ActivityRestaurantDetailBinding restaurantDetailBinding = DataBindingUtil.setContentView(this, this.getActivityLayout());
        restaurantViewModel = new RestaurantViewModel(this, this.getIdFromIntentData());
        restaurantDetailBinding.setRestaurantViewModel(restaurantViewModel);
        ButterKnife.bind(this);
    }

    /**
     * Configure recycler view.
     */
    private void configureRecyclerView() {
        // Create adapter passing in the sample user data
        this.adapter = new RestaurantAdapter(this.bookedUserId, Glide.with(this), this);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(this.adapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Configure swipe refresh layout.
     */
    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            restaurantViewModel.restartRequest();
            this.getRestaurantBookedUsers();
        });
    }

    // --------------
    // ACTION
    // --------------

//    @OnClick(R.id.activity_restaurant_detail_button_booked)
//    protected void onClickBookRestaurant() {
//        ObservableField<Place> placeRestaurant = restaurantViewModel.getPlace();
//
//        if (placeRestaurant.get() != null && placeRestaurant.get().getId() != null) {
//            // Get old booked restaurant before update.
//            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
//                String oldBookedRestaurant = documentSnapshot.toObject(User.class).getBookedRestaurantId();
//                if (!oldBookedRestaurant.equals(placeRestaurant.get().getId())) {// TODO and not already booked + toast or snackbar
//                    // Remove user id of old booked restaurant.
//                    RestaurantHelper.removeBookedUser(oldBookedRestaurant, getCurrentUser().getUid());
//
//                    // If restaurant isn't booked, remove from firestore.
//                    RestaurantHelper.getRestaurant(oldBookedRestaurant).addOnSuccessListener(documentSnapshot1 -> {
//                        if (documentSnapshot1.toObject(Restaurant.class).getBookedUsersId().isEmpty())
//                            RestaurantHelper.deleteRestaurant(oldBookedRestaurant);
//                    });
//                }
//            });
//
//            // Update user's bookedRestaurantId in firestore.
//            UserHelper.updateBookedRestaurant(this.getCurrentUser().getUid(), placeRestaurant.get().getId());
//
//            // Update restaurant in firestore.
//            List<String> userList = new ArrayList<>();
//            userList.add(this.getCurrentUser().getUid());
//
//            if (restaurant == null)
//                RestaurantHelper.createRestaurant(placeRestaurant.get().getId(), placeRestaurant.get().getName(), userList,
//                        placeRestaurant.get().getOpeningHours().toString(), placeRestaurant.get().getTypes().toString(),
//                        placeRestaurant.get().getRating());
//            else
//                RestaurantHelper.updateBookedUsers(restaurant.getId(), this.getCurrentUser().getUid());
//        }
//    }

    @OnClick(R.id.activity_restaurant_detail_button_booked)
    protected void onClickBookRestaurant() {
        Place currentRestaurant = restaurantViewModel.getPlace().get();

        if (currentRestaurant != null && currentRestaurant.getId() != null) {
            UserDataBaseViewModel userDataBaseViewModel = new UserDataBaseViewModel(this.getCurrentUser().getUid());
            userDataBaseViewModel.updateBookedRestaurant(currentRestaurant);
            this.getRestaurantBookedUsers();
        } else Snackbar.make(swipeRefreshLayout, R.string.activity_restaurant_detail_no_restaurant_data, Snackbar.LENGTH_SHORT).show();
    }

    // --------------
    // UI
    // --------------

    /**
     * Stop swipe refresh animation.
     */
    public void hideSwipeRefresh() { this.swipeRefreshLayout.setRefreshing(false); }

    /**
     * Update recycler view.
     */
    public void updateRecyclerView() {
        if (!bookedUserId.isEmpty()) {
            this.recyclerView.setVisibility(View.VISIBLE);
            if (this.adapter == null) this.configureRecyclerView();
            else this.adapter.notifyDataSetChanged();
        } else this.recyclerView.setVisibility(View.GONE);
    }

    // --- SETTERS ---

    /**
     * Setter for booked users of restaurant.
     * @param bookedUsersId List of booked users id.
     */
    public void setBookedUserId(List<String> bookedUsersId) {
        this.bookedUserId = bookedUsersId;
    }
}
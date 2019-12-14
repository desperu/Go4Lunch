package org.desperu.go4lunch.view.restaurantdetail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.snackbar.Snackbar;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.view.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityRestaurantDetailBinding;
import org.desperu.go4lunch.view.adapter.RestaurantDetailAdapter;
import org.desperu.go4lunch.viewmodel.RestaurantDBViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.desperu.go4lunch.Go4LunchTools.CodeResponse.*;

public class RestaurantDetailActivity extends BaseActivity {

    //FOR DESIGN
    @BindView(R.id.activity_restaurant_detail_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.activity_restaurant_detail_swipe_container) SwipeRefreshLayout swipeRefreshLayout;

    // FOR DATA
    public static final String RESTAURANT_ID = "restaurant id";

    private RestaurantViewModel restaurantViewModel;
    private RestaurantDetailAdapter adapter;
    private List<UserDBViewModel> joiningUsers = new ArrayList<>();

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
        this.configureRecyclerView();
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
        RestaurantDBViewModel restaurantDBViewModel =
                new RestaurantDBViewModel(this.getIdFromIntentData());
        restaurantDBViewModel.getRestaurant(this);
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
        this.adapter = new RestaurantDetailAdapter(R.layout.fragment_restaurant_detail_item, joiningUsers);
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

    @OnClick(R.id.activity_restaurant_detail_button_booked)
    protected void onClickBookRestaurant() {
        Place currentRestaurant = restaurantViewModel.getPlace().get();

        if (currentRestaurant != null && currentRestaurant.getId() != null) {
            UserDBViewModel userDBViewModel = new UserDBViewModel(this, this.getCurrentUser().getUid());
            userDBViewModel.updateBookedRestaurant(this, currentRestaurant);
            // Wait before reload actualised data from firestore.
            new Handler().postDelayed(this::getRestaurantBookedUsers, 1500);
        } else this.handleResponseAfterBooking(NO_DATA);
    }

    @OnClick(R.id.activity_restaurant_detail_call_button)
    protected void onClickCallRestaurant() {
        if (restaurantViewModel.getPlace().get() != null)
            this.startCallIntent(restaurantViewModel.getPlace().get().getPhoneNumber());
        else this.handleResponseAfterBooking(NO_DATA);
    }

    @OnClick(R.id.activity_restaurant_detail_like_button)
    protected void onClickLikeRestaurant() {
        //TODO send like to google...
    }

    @OnClick(R.id.activity_restaurant_detail_website_button)
    protected void onClickWebsiteRestaurant() {
        if (restaurantViewModel.getPlace().get() != null)
            this.showWebsite(restaurantViewModel.getPlace().get().getWebsiteUri().toString());
        else this.handleResponseAfterBooking(NO_DATA);
    }

    // --------------
    // ACTIVITIES
    // --------------

    /**
     * Start call action.
     * @param phone Phone number.
     */
    @SuppressLint("MissingPermission") // TODO remove and ask for permission...
    private void startCallIntent(String phone) {
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phone)));
    }

    /**
     * Show website.
     * @param url Web url.
     */
    private void showWebsite(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(url), "text/html"));
    }

    // --------------
    // UI
    // --------------

    /**
     * Update recycler view when received data.
     * @param restaurant Restaurant from firestore.
     */
    public void updateRecyclerView(Restaurant restaurant) {
        joiningUsers.clear();
        if (restaurant != null) {
            for (String user : restaurant.getBookedUsersId()) {
                UserDBViewModel userDBViewModel = new UserDBViewModel(this, user);
                userDBViewModel.fetchUser();
                joiningUsers.add(userDBViewModel);
            }
        }
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    /**
     * Show Snackbar with corresponding message.
     * @param message Message to show.
     */
    private void showSnackbar(String message){
        Snackbar.make(swipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    // --------------
    // UTILS
    // --------------

    /**
     * Handle response after booked button clicked.
     * @param resultCode Code result from request method.
     */
    public void handleResponseAfterBooking(int resultCode) {
        switch (resultCode) {
            case BOOKED:
                this.showSnackbar(getString(R.string.activity_restaurant_detail_restaurant_booked));
                break;
            case UNBOOKED:
                this.showSnackbar(getString(R.string.activity_restaurant_detail_restaurant_unbooked));
                break;
            case ERROR:
                this.showSnackbar(getString(R.string.error_unknown_error));
                break;
            case NO_DATA:
                this.showSnackbar(getString(R.string.activity_restaurant_detail_no_restaurant_data));
                break;
            default:
                break;
        }
    }
}
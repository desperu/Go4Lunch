package org.desperu.go4lunch.view.restaurantdetail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static org.desperu.go4lunch.Go4LunchTools.CodeResponse.*;
import static org.desperu.go4lunch.Go4LunchTools.RestaurantDetail.*;

public class RestaurantDetailActivity extends BaseActivity {

    //FOR DESIGN
    @BindView(R.id.activity_restaurant_detail_swipe_container) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.activity_restaurant_detail_recycler_view) RecyclerView recyclerView;

    // FOR DATA
    public static final String RESTAURANT_ID = "restaurant id";

    private RestaurantInfoViewModel restaurantInfoViewModel;
    private RestaurantDBViewModel restaurantDBViewModel;
    private Place restaurantInfo;
    private RestaurantDetailAdapter adapter;
    private List<UserDBViewModel> joiningUsers = new ArrayList<>();
    private Restaurant restaurantDB;
    private boolean isCallPermissionEnabled = false;
    private boolean isAlreadyClicked = false;

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
        this.observeRestaurantData();
        this.checkCallPhonePermissionsStatus();
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
     * Configure data binding.
     */
    private void configureDataBinding() {
        assert getCurrentUser() != null;
        ActivityRestaurantDetailBinding restaurantDetailBinding = DataBindingUtil.setContentView(this, this.getActivityLayout());
        restaurantInfoViewModel = new RestaurantInfoViewModel(getApplication(), this.getIdFromIntentData());
        restaurantDetailBinding.setRestaurantInfoViewModel(restaurantInfoViewModel);

        restaurantDBViewModel = new RestaurantDBViewModel(getApplication(), this.getIdFromIntentData());
        restaurantDBViewModel.fetchRestaurant();
        restaurantDBViewModel.setUserId(this.getCurrentUser().getUid());
        restaurantDetailBinding.setRestaurantDBViewModel(restaurantDBViewModel);
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
            swipeRefreshLayout.setEnabled(recyclerView.getScrollY() == 0);
            restaurantInfoViewModel.restartRequest();
            this.reloadRestaurantData();
        });
    }

    /**
     * Check if call phone permission is granted, if not, ask for it.
     */
    private void checkCallPhonePermissionsStatus() {
        if (!EasyPermissions.hasPermissions(this, PERMS))
            EasyPermissions.requestPermissions(this, getString(R.string.activity_restaurant_detail_popup_title_permission_call),
                    PERM_CALL_PHONE, PERMS);
        else isCallPermissionEnabled = true;
    }

    // --------------
    // METHODS OVERRIDE
    // --------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        this.isCallPermissionEnabled = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    // --------------
    // ACTION
    // --------------

    @OnClick(R.id.activity_restaurant_detail_button_booked)
    protected void onClickBookRestaurant() {
        assert this.getCurrentUser() != null;
        if (restaurantInfo == null) restaurantInfo = restaurantInfoViewModel.getPlace().get();

        if (restaurantInfo != null && restaurantInfo.getId() != null) {
            UserDBViewModel userDBViewModel = new UserDBViewModel(getApplication(), this.getCurrentUser().getUid());
            userDBViewModel.updateBookedRestaurant(restaurantInfo, restaurantDB);
            userDBViewModel.getUpdateBookedResponse().observe(this, this::handleResponseAfterAction);
            // Wait before reload actualised data from firestore.
            new Handler().postDelayed(this::reloadRestaurantData, 1500);
        } else this.handleResponseAfterAction(NO_DATA);
    }

    @OnClick(R.id.activity_restaurant_detail_call_button)
    protected void onClickCallRestaurant() {
        if (restaurantInfo == null) restaurantInfo = restaurantInfoViewModel.getPlace().get();

        if (restaurantInfo != null)
            if (isCallPermissionEnabled)
                this.startCallIntent(restaurantInfo.getPhoneNumber());
            else this.checkCallPhonePermissionsStatus();
        else this.handleResponseAfterAction(NO_DATA);
    }

    @OnClick(R.id.activity_restaurant_detail_like_button)
    protected void onClickLikeRestaurant() {
        assert this.getCurrentUser() != null;
        boolean isAlreadyLike = false;
        if (restaurantInfo == null) restaurantInfo = restaurantInfoViewModel.getPlace().get();

        if (restaurantInfo != null) {
            // Check if current user already like restaurant
            if (restaurantDB != null && restaurantDB.getLikeUsers() != null) {
                for (String userId : restaurantDB.getLikeUsers())
                    isAlreadyLike = userId.equals(this.getCurrentUser().getUid());
            }

            if (!isAlreadyLike) {
                // Like restaurant
                restaurantDBViewModel.updateRestaurantLikeUsers(restaurantInfo, this.getCurrentUser().getUid());
                this.showSnackbar(getString(R.string.activity_restaurant_detail_snackbar_add_like));
                this.reloadRestaurantData();
            } else {
                if (isAlreadyClicked) {
                    // Unlike restaurant
                    restaurantDBViewModel.removeRestaurantLikeUser(restaurantInfo.getId(), this.getCurrentUser().getUid());
                    this.showSnackbar(getString(R.string.activity_restaurant_detail_snackbar_remove_like));
                    this.reloadRestaurantData();
                } else {
                    // Show message for two click to unlike
                    Toast.makeText(this, R.string.activity_restaurant_detail_snackbar_already_like, Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> isAlreadyClicked = false, 2000);
                    this.isAlreadyClicked = true;
                }
            }
        } else this.handleResponseAfterAction(NO_DATA);
    }

    @OnClick(R.id.activity_restaurant_detail_website_button)
    protected void onClickWebsiteRestaurant() {
        if (restaurantInfo == null) restaurantInfo = restaurantInfoViewModel.getPlace().get();

        if (restaurantInfo != null && restaurantInfo.getWebsiteUri() != null)
            this.showWebsite(restaurantInfo.getWebsiteUri().toString());
        else this.handleResponseAfterAction(NO_DATA);
    }

    // --------------
    // ACTIVITIES
    // --------------

    /**
     * Start call action.
     * @param phone Phone number.
     */
    @SuppressLint("MissingPermission")
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
    // REQUEST & DATA
    // --------------

    /**
     * Reload restaurant data from firestore.
     */
    private void reloadRestaurantData() { restaurantDBViewModel.fetchRestaurant(); }

    /**
     * Observe restaurant data, and update data and ui when received data.
     */
    private void observeRestaurantData() {
        restaurantDBViewModel.getRestaurantLiveData().observe(this, restaurant -> {
            this.restaurantDB = restaurant;
            this.updateRecyclerView(restaurant);
        });
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
                UserDBViewModel userDBViewModel = new UserDBViewModel(getApplication(), user);
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
     * Handle response after action button clicked.
     * @param resultCode Code result from request method.
     */
    private void handleResponseAfterAction(int resultCode) {
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
package org.desperu.go4lunch.view.main;

import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.databinding.ActivityMainNavHeaderBinding;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.notifications.NotificationAlarmManager;
import org.desperu.go4lunch.utils.Go4LunchPrefs;
import org.desperu.go4lunch.view.base.BaseActivity;
import org.desperu.go4lunch.view.main.fragments.ChatFragment;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.main.fragments.RestaurantListFragment;
import org.desperu.go4lunch.view.main.fragments.WorkmatesFragment;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;
import org.desperu.go4lunch.view.settings.SettingsActivity;
import org.desperu.go4lunch.viewmodel.UserAuthViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Objects;

import butterknife.BindView;
import icepick.State;

import static org.desperu.go4lunch.Go4LunchTools.FragmentKey.*;
import static org.desperu.go4lunch.Go4LunchTools.PrefsKeys.*;
import static org.desperu.go4lunch.Go4LunchTools.SettingsDefault.*;
import static org.desperu.go4lunch.view.main.fragments.MapsFragment.*;
import static org.desperu.go4lunch.view.main.fragments.RestaurantListFragment.*;
import static org.desperu.go4lunch.view.main.fragments.WorkmatesFragment.QUERY_TERM_WORKMATES;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener, MapsFragment.OnNewDataOrClickListener,
        RestaurantListFragment.OnNewDataListener, RestaurantListFragment.OnClickListener {

    // FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.toolbar_search_view) SearchView searchView;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_nav_bottom) BottomNavigationView bottomNavigationView;

    // FOR DATA
    private ActivityMainNavHeaderBinding activityMainNavHeaderBinding;
    private UserAuthViewModel userAuthViewModel;
    private UserDBViewModel userDBViewModel;
    @State int currentFragment = -1;
    private Fragment fragment;
    private static final int RC_SIGN_IN = 1234;

    // FOR FRAGMENTS COMMUNICATION
    @State RectangularBounds nearbyBounds;
    @State Location userLocation;
    @State ArrayList<String> placeIdList;
    @State RectangularBounds bounds;
    @State CameraPosition cameraPosition;
    @State String queryTerm;
    @State boolean isQueryForRestaurant = false;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getActivityLayout() { return R.layout.activity_main; }

    @Override
    protected void configureDesign() {
        this.configureToolBar();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureBottomNavigationView();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    /**
     * Configure Drawer layout.
     */
    private void configureDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Configure Navigation Drawer.
     */
    private void configureNavigationView() {
        // Support status bar for KitKat.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            navigationView.setPadding(0, 0, 0, 0);
        navigationView.setNavigationItemSelectedListener(this);
        // Disable check item
        navigationView.getMenu().setGroupCheckable(R.id.activity_main_menu_drawer_group, false, false);
    }

    /**
     * Configure Bottom Navigation View.
     */
    private void configureBottomNavigationView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    /**
     * Configure and show corresponding fragment.
     * @param fragmentKey Key for fragment.
     */
    private void configureAndShowFragment(int fragmentKey) {
        String titleActivity = getString(R.string.title_activity_main);
        Bundle bundle = new Bundle();

        fragment = getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_frame_layout);

        if (currentFragment != fragmentKey) {
            switch (fragmentKey) {
                case MAP_FRAGMENT:
                    fragment = MapsFragment.newInstance();
                    bundle.putStringArrayList(PLACE_ID_LIST_MAPS, placeIdList);
                    if (this.bounds != null)
                        bundle.putParcelable(CAMERA_POSITION, cameraPosition);
                    if (this.queryTerm != null && !this.queryTerm.isEmpty())
                        bundle.putString(QUERY_TERM_MAPS, queryTerm);
                    bundle.putBoolean(MapsFragment.IS_QUERY_FOR_RESTAURANT_MAPS, isQueryForRestaurant);
                    fragment.setArguments(bundle);
                    break;
                case LIST_FRAGMENT:
                    fragment = RestaurantListFragment.newInstance();
                    bundle.putStringArrayList(PLACE_ID_LIST_RESTAURANT_LIST, placeIdList);
                    bundle.putParcelable(BOUNDS, bounds);
                    if (this.queryTerm != null && !this.queryTerm.isEmpty())
                        bundle.putString(QUERY_TERM_LIST, queryTerm);
                    bundle.putBoolean(IS_QUERY_FOR_RESTAURANT_LIST, isQueryForRestaurant);
                    bundle.putParcelable(NEARBY_BOUNDS, nearbyBounds);
                    bundle.putParcelable(USER_LOCATION, userLocation);
                    fragment.setArguments(bundle);
                    break;
                case WORKMATES_FRAGMENT:
                    fragment = WorkmatesFragment.newInstance();
                    if (this.queryTerm != null && !this.queryTerm.isEmpty())
                        bundle.putString(QUERY_TERM_WORKMATES, queryTerm);
                    fragment.setArguments(bundle);
                    titleActivity = getString(R.string.title_fragment_workmates);
                    break;
                case CHAT_FRAGMENT:
                    fragment = ChatFragment.newInstance();
                    this.hideSearchViewIfVisible();
                    titleActivity = getString(R.string.title_fragment_chat);
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_frame_layout, fragment)
                    .commit();

            this.setTitleActivity(titleActivity);
            if (toolbar.findViewById(R.id.activity_main_menu_search) != null)
                toolbar.findViewById(R.id.activity_main_menu_search).setVisibility(
                    fragmentKey != CHAT_FRAGMENT ? View.VISIBLE : View.GONE);
        }
        currentFragment = fragmentKey;
    }

    /**
     * Configure data binding for Navigation View Header with user info.
     */
    private void configureDataBindingForHeader() {
        // Enable Data binding for user info
        if (activityMainNavHeaderBinding == null) {
            View headerView = navigationView.getHeaderView(0);
            activityMainNavHeaderBinding = ActivityMainNavHeaderBinding.bind(headerView);
        }
        userAuthViewModel = new UserAuthViewModel();
        activityMainNavHeaderBinding.setUserAuthViewModel(userAuthViewModel);
    }

    /**
     * Enable notification at first apk start.
     */
    private void enableNotifications() { // TODO if notification didn't sent use handler to resent and marker is_notification_sent
        if (Go4LunchPrefs.getBoolean(getBaseContext(), IS_FIRST_APK_START, FIRST_APK_START_DEFAULT)) {
            NotificationAlarmManager.startNotificationsAlarm(getBaseContext());
            Go4LunchPrefs.savePref(getBaseContext(), IS_FIRST_APK_START, false);
        }
    }

    // -----------------
    // METHODS OVERRIDE
    // -----------------


    @Override
    protected void onResume() {
        super.onResume();
        if (this.isCurrentUserLogged()) {
            this.configureAndShowFragment(this.currentFragment >= 0 ? currentFragment : MAP_FRAGMENT);
            this.configureDataBindingForHeader();
            this.loadOrCreateUserInFirestore();
            this.enableNotifications();
            this.manageResetBookedRestaurant();
        }
        else this.startSignInActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Handle SignIn Activity response on activity result.
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
                // Menu drawer
            case R.id.activity_main_menu_drawer_your_lunch:
                this.onClickYourLunch();
                break;
            case R.id.activity_main_menu_drawer_settings:
                this.showSettingsActivity();
                break;
            case R.id.activity_main_menu_drawer_log_out:
                this.logOut();
                break;
                // Bottom Navigation
            case R.id.activity_main_menu_bottom_map:
                this.configureAndShowFragment(MAP_FRAGMENT);
                break;
            case R.id.activity_main_menu_bottom_list:
                this.configureAndShowFragment(LIST_FRAGMENT);
                break;
            case R.id.activity_main_menu_bottom_workmates:
                this.configureAndShowFragment(WORKMATES_FRAGMENT);
                break;
            case R.id.activity_main_menu_bottom_chat:
                this.configureAndShowFragment(CHAT_FRAGMENT);
                break;
            default:
                break;
        }
        this.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START))
            this.drawerLayout.closeDrawer(GravityCompat.START);
        else if (this.searchView != null && this.searchView.isShown()) {
            this.hideSearchViewIfVisible();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onSearchTextChange(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                onSearchTextChange(s);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.activity_main_menu_search) {
            searchView.setVisibility(View.VISIBLE);
            searchView.onActionViewExpanded();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // --------------------
    // ACTION
    // --------------------

    @Override
    public void onClickedMarker(String id) { this.showRestaurantDetailActivity(id); }

    @Override
    public void onNewQuerySearch(boolean isQueryForRestaurant) { this.isQueryForRestaurant = isQueryForRestaurant; }

    @Override
    public void saveNearbyBounds(RectangularBounds nearbyBounds) { this.nearbyBounds = nearbyBounds; }

    @Override
    public void onNewUserLocation(Location userLocation) { this.userLocation = userLocation; }

    @Override
    public void onNewPlacesIdList(ArrayList<String> placeIdList) { this.placeIdList = placeIdList; }

    @Override
    public void onNewBounds(RectangularBounds bounds) { this.bounds = bounds; }

    @Override
    public void onNewCameraPosition(CameraPosition cameraPosition) { this.cameraPosition = cameraPosition; }

    @Override
    public void onItemClick(String restaurantId) { this.showRestaurantDetailActivity(restaurantId); }

    /**
     * Manage click on Your Lunch button.
     */
    private void onClickYourLunch() {
        if (userDBViewModel.getUser().get() != null
                && Objects.requireNonNull(userDBViewModel.getUser().get()).getBookedRestaurantId() != null)
            this.showRestaurantDetailActivity(Objects.requireNonNull(userDBViewModel.getUser().get()).getBookedRestaurantId());
        else Snackbar.make(drawerLayout, R.string.activity_main_message_no_booked_restaurant, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Fetch restaurant on search text change, and send query term to corresponding fragment.
     * @param query Query term to search.
     */
    private void onSearchTextChange(String query) {
        this.queryTerm = query;
        if (fragment.getClass() == MapsFragment.class) {
            isQueryForRestaurant = true;
            MapsFragment mapsFragment = (MapsFragment) this.fragment;
            mapsFragment.onSearchQueryTextChange(query);
        } else if (fragment.getClass() == RestaurantListFragment.class) {
            isQueryForRestaurant = true;
            RestaurantListFragment restaurantListFragment = (RestaurantListFragment) this.fragment;
            restaurantListFragment.onSearchQueryTextChange(query);
        } else if (fragment.getClass() == WorkmatesFragment.class) {
            isQueryForRestaurant = false;
            WorkmatesFragment workmatesFragment = (WorkmatesFragment) this.fragment;
            workmatesFragment.onSearchQueryTextChange(query);
        }
    }

    // -----------------
    // ACTIVITY
    // -----------------

    /**
     * Start restaurant detail activity.
     * @param id Place id.
     */
    private void showRestaurantDetailActivity(String id) {
        startActivity(new Intent(this, RestaurantDetailActivity.class).putExtra(RestaurantDetailActivity.RESTAURANT_ID, id));
    }

    /**
     * Start settings activity.
     */
    private void showSettingsActivity() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    // --------------------
    // LOGIN
    // --------------------

    /**
     * Launch sign in activity with firesbase ui.
     */
    private void startSignInActivity(){
        AuthMethodPickerLayout customLayout = new AuthMethodPickerLayout.Builder(R.layout.activity_login)
                .setEmailButtonId(R.id.activity_login_button_email)
                .setGoogleButtonId(R.id.activity_login_button_google)
                .setFacebookButtonId(R.id.activity_login_button_facebook)
                .setTwitterButtonId(R.id.activity_login_button_twitter)
                .build();

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(Arrays.asList(
                                // Email authentication
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                // Google authentication
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                // Facebook authentication
                                new AuthUI.IdpConfig.FacebookBuilder().build(),
                                // Twitter authentication
                                new AuthUI.IdpConfig.TwitterBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setAuthMethodPickerLayout(customLayout)
                        .build(),
                RC_SIGN_IN);
    }

    /**
     * Log out of current login, and start Login Activity.
     */
    private void logOut() {
        userAuthViewModel.userLogOut();
        this.startSignInActivity();
    }

    // --------------------
    // FIRESTORE
    // --------------------

    /**
     * Load or create user in firestore.
     */
    private void loadOrCreateUserInFirestore() {
        userDBViewModel = new UserDBViewModel(getApplication(), userAuthViewModel.getUid());
        userDBViewModel.fetchUser();
        userDBViewModel.getUserLiveData().observe(this, user -> {
            if (user == null)
                userDBViewModel.createUserInFirestore(userAuthViewModel.getUid(),
                        userAuthViewModel.getUserName(), userAuthViewModel.getUserPicture());
        });
    }

    // -----------------
    // UI
    // -----------------

    /**
     * Set title activity name.
     * @param titleActivity Fragment title.
     */
    private void setTitleActivity(String titleActivity) { this.setTitle(titleActivity); }

    /**
     * Hide search view if visible, and clear query.
     */
    private void hideSearchViewIfVisible() {
        if (searchView != null && searchView.isShown()) {
            this.searchView.setVisibility(View.GONE);
            this.searchView.setQuery(null, true);
        }
    }

    /**
     * Show Toast with corresponding message.
     * @param message Message to show.
     */
    private void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // --------------------
    // UTILS
    // --------------------

    /**
     * Method that handles response after SignIn Activity close.
     * @param requestCode Code of the request.
     * @param resultCode Code result from sign in activity.
     * @param data Data from sign in activity.
     */
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) { // SUCCESS
                showToast(getString(R.string.connection_succeed));
            } else { // ERRORS
                if (response == null) {
                    showToast(getString(R.string.error_authentication_canceled));
                    this.finishAffinity();
                } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showToast(getString(R.string.error_no_internet));
                    this.startSignInActivity();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    showToast(getString(R.string.error_unknown_error));
                    this.startSignInActivity();
                }
            }
        }
    }

    /**
     * Manage reset booked restaurant function.
     */
    private void manageResetBookedRestaurant() {
        if (Go4LunchPrefs.getBoolean(getBaseContext(), RESET_BOOKED_RESTAURANT, RESET_BOOKED_DEFAULT)) {
            boolean isNotificationSent = Go4LunchPrefs.getBoolean(getBaseContext(),
                    IS_BOOKED_NOTIFICATION_SENT, BOOKED_NOTIFICATION_SENT_DEFAULT);

            // Set calendar with booked time
            long bookedTime = Go4LunchPrefs.getLong(getBaseContext(),
                    BOOKED_RESTAURANT_TIME, BOOKED_TIME_DEFAULT);
            Calendar bookedCal = Calendar.getInstance();
            bookedCal.setTimeInMillis(bookedTime);

            // Set calendar with current time
            Calendar resetBookedCal = Calendar.getInstance();
            resetBookedCal.set(Calendar.HOUR_OF_DAY, 14);
            resetBookedCal.set(Calendar.MINUTE, 30);

            // If notification was sent, booked time not null,
            // and current time over 2.30pm, reset booked restaurant
            if (isNotificationSent && bookedTime != 0 && resetBookedCal.compareTo(bookedCal) > 0) {
                userDBViewModel.updateBookedRestaurant(Place.builder().build(),
                        new Restaurant(RESET_BOOKED_RESTAURANT, "",
                                new ArrayList<>(), 0.0,new ArrayList<>()));
                Go4LunchPrefs.clear(getBaseContext(), IS_BOOKED_NOTIFICATION_SENT);
                Go4LunchPrefs.clear(getBaseContext(), BOOKED_RESTAURANT_TIME);
            }
        }
    }
}
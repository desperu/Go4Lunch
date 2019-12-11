package org.desperu.go4lunch.view.main;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.UserHelper;
import org.desperu.go4lunch.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityMainNavHeaderBinding;
import org.desperu.go4lunch.view.TestBindingActivity;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;
import org.desperu.go4lunch.viewmodel.UserAuthViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener, MapsFragment.OnMarkerClickedListener {

    // FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.toolbar_autocomplete) RelativeLayout autoCompleteContainer;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_nav_bottom) BottomNavigationView bottomNavigationView;

    // FOR DATA
    private UserAuthViewModel userAuthViewModel;
    private AutocompleteSupportFragment autocompleteFragment;
    private static final int RC_SIGN_IN = 1234;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getActivityLayout() { return R.layout.activity_main; }

    @Override
    protected void configureDesign() {
        this.configureToolBar();
        this.setTitleActivity();
        this.configureDrawerLayout();
        this.configureNavigationView();
        this.configureBottomNavigationView();
        this.updateHeaderWithUserInfo();
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
        //TODO add method to select the item when back from an activity. (onUserInteraction)
    }

    /**
     * Configure and show corresponding fragment.
     */
    private void configureAndShowFragment() {
//        Fragment fragment = frag;

        MapsFragment fragment = (MapsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_frame_layout);

        if (fragment == null) {
            fragment = MapsFragment.newInstance();
//            Bundle bundle = new Bundle();
//            bundle.putInt(KEY_FRAGMENT, NOTIFICATION_FRAGMENT);
//            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.activity_main_frame_layout, fragment)
                    .commit();
        }
    }

    /**
     * Configure places autocomplete search.
     */
    private void configureAutocomplete() {
        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.toolbar_autocomplete);

        if (autocompleteFragment == null) {
            autocompleteFragment = AutocompleteSupportFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.toolbar_autocomplete, autocompleteFragment)
                    .commit();
        }

//        TypeFilter typeFilter = TypeFilter.Creator<Place.Type.RESTAURANT>;
//        autocompleteFragment.setTypeFilter(typeFilter);

        // TODO put int a ViewModel
        Places.initialize(this, BuildConfig.google_maps_api_key);

//        PlacesClient placesClient = Places.createClient(this);
        autocompleteFragment.setHint(getString(R.string.activity_main_search_edit_text_hint));
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                // TODO: Get info about the selected place.
                Toast.makeText(getBaseContext(),"place" + place.getName(), Toast.LENGTH_SHORT).show();
                Log.i("MainActivity", "Place: " + place.getName() + ", " + place.getId());
            }

            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Toast.makeText(getBaseContext(), "Error" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                Log.i("MainActivity", "An error occurred: " + status);
            }
        });
    }

    // -----------------
    // METHODS OVERRIDE
    // -----------------


    @Override
    protected void onResume() {
        super.onResume();
        if (this.isCurrentUserLogged()) this.configureAndShowFragment();
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
//                this.showSearchArticlesActivity();
                break;
            case R.id.activity_main_menu_drawer_settings:
//                this.showNotificationsActivity();
                break;
            case R.id.activity_main_menu_drawer_log_out:
                this.logOut();
                break;
                // Bottom Navigation
            case R.id.activity_main_menu_bottom_map:
                this.configureAndShowFragment();
                break;
            case R.id.activity_main_menu_bottom_list:
                Toast.makeText(this, "test list", Toast.LENGTH_SHORT).show();
//                this.showAboutDialog();
                break;
            case R.id.activity_main_menu_bottom_workmates:
//                this.showHelpDocumentation();
                // TODO for test only
                startActivity(new Intent(this, TestBindingActivity.class));
                break;
            case R.id.activity_main_menu_bottom_chat:
                Toast.makeText(this, "test chat", Toast.LENGTH_SHORT).show();
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
        else if (autoCompleteContainer != null && autoCompleteContainer.isShown()) {
            if (autocompleteFragment != null && autocompleteFragment.isVisible())
                getSupportFragmentManager().beginTransaction().remove(autocompleteFragment).commit();
            autoCompleteContainer.setVisibility(View.GONE);
        }
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        if (item.getItemId() == R.id.activity_main_menu_search) {
            autoCompleteContainer.setVisibility(View.VISIBLE);
            this.configureAutocomplete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -----------------
    // UI
    // -----------------

    /**
     * Set title activity name.
     */
    private void setTitleActivity() {
        this.setTitle(R.string.title_activity_main);
    }

    /**
     * Update Navigation View Header with user info.
     */
    private void updateHeaderWithUserInfo() {
        if (isCurrentUserLogged()) {
            // Enable Data binding for user info
            View headerView = navigationView.getHeaderView(0);
            ActivityMainNavHeaderBinding activityMainNavHeaderBinding = ActivityMainNavHeaderBinding.bind(headerView);
            userAuthViewModel = new UserAuthViewModel();
            activityMainNavHeaderBinding.setUserAuthViewModel(userAuthViewModel);
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
    // ACTION
    // --------------------

    @Override
    public void onClickedMarker(String id) {
        this.showRestaurantDetailActivity(id);
    }

    // -----------------
    // ACTIVITY
    // -----------------

//    /**
//     * Start show article activity.
//     *
//     * @param articleUrl The url's article.
//     */
//    private void showArticleActivity(String articleUrl) {
//        startActivity(new Intent(this, ShowArticleActivity.class).putExtra(ShowArticleActivity.ARTICLE_URL, articleUrl));
//    }
//
//    /**
//     * Start search articles activity.
//     */
//    private void showSearchArticlesActivity() {
//        startActivity(new Intent(this, SearchArticlesActivity.class));
//    }
//
//    /**
//     * Start notifications activity.
//     */
//    private void showNotificationsActivity() {
//        startActivity(new Intent(this, NotificationsActivity.class));
//    }

    /**
     * Start restaurant detail activity.
     * @param id Place id.
     */
    private void showRestaurantDetailActivity(String id) {
        startActivity(new Intent(this, RestaurantDetailActivity.class).putExtra(RestaurantDetailActivity.RESTAURANT_ID, id));
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
     * Create user in firestore.
     */
    private void createUserInFirestore(){
        if (this.getCurrentUser() != null)
            UserHelper.createUser(userAuthViewModel.getUid(), userAuthViewModel.getUserName(), userAuthViewModel.getUserPicture())
                    .addOnFailureListener(this.onFailureListener());
    }

    // --------------------
    // UTILS
    // --------------------

    //TODO not good not show
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
                if (userAuthViewModel == null) this.configureDesign();
                this.createUserInFirestore();
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
}
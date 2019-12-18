package org.desperu.go4lunch.view.main;

import android.content.Intent;
import android.os.Build;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.databinding.ActivityMainNavHeaderBinding;
import org.desperu.go4lunch.view.base.BaseActivity;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.main.fragments.RestaurantListFragment;
import org.desperu.go4lunch.view.main.fragments.WorkmatesFragment;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;
import org.desperu.go4lunch.viewmodel.AutocompleteViewModel;
import org.desperu.go4lunch.viewmodel.UserAuthViewModel;
import org.desperu.go4lunch.viewmodel.UserDBViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

import butterknife.BindView;

import static org.desperu.go4lunch.Go4LunchTools.FragmentKey.*;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener, MapsFragment.OnMarkerClickedListener {

    // FOR DESIGN
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.toolbar_search_view) SearchView searchView;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_nav_bottom) BottomNavigationView bottomNavigationView;

    // FOR DATA
    private UserAuthViewModel userAuthViewModel;
    private UserDBViewModel userDBViewModel;
    private Fragment fragment;
    private static final int RC_SIGN_IN = 1234;
    private int currentFragment = -1;

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
        this.configureDataBindingForHeader();
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
     * @param fragmentKey Key for fragment.
     */
    private void configureAndShowFragment(int fragmentKey) {
        String titleActivity = getString(R.string.title_activity_main);

        fragment = getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_frame_layout);

        if (currentFragment != fragmentKey) {
            switch (fragmentKey) {
                case MAP_FRAGMENT:
                    fragment = MapsFragment.newInstance();
                    break;
                case LIST_FRAGMENT:
                    fragment = RestaurantListFragment.newInstance();
                    break;
                case WORKMATES_FRAGMENT:
                    fragment = WorkmatesFragment.newInstance();
                    titleActivity = getString(R.string.title_fragment_workmates);
                    break;
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_frame_layout, fragment)
                    .commit();

            this.setTitleActivity(titleActivity);
        }
        currentFragment = fragmentKey;
    }

    /**
     * Configure data binding for Navigation View Header with user info.
     */
    private void configureDataBindingForHeader() {
        if (isCurrentUserLogged()) {
            // Enable Data binding for user info
            View headerView = navigationView.getHeaderView(0);
            ActivityMainNavHeaderBinding activityMainNavHeaderBinding = ActivityMainNavHeaderBinding.bind(headerView);
            userAuthViewModel = new UserAuthViewModel();
            activityMainNavHeaderBinding.setUserAuthViewModel(userAuthViewModel);
        }
    }

    // -----------------
    // METHODS OVERRIDE
    // -----------------


    @Override
    protected void onResume() {
        super.onResume();
        if (this.isCurrentUserLogged()) {
            this.configureAndShowFragment(MAP_FRAGMENT);
            this.loadUserDataFromFirestore();
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
//                this.showNotificationsActivity();
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
        else if (this.searchView != null && this.searchView.isShown())
            this.searchView.setVisibility(View.GONE);
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(getApplicationContext(), fragment);
                if (fragment.getClass() == MapsFragment.class) {
                    MapsFragment mapsFragment = (MapsFragment) fragment;
                    autocompleteViewModel.fetchAutocompletePrediction(query, mapsFragment.getRectangularBounds());
                }
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(getApplicationContext(), fragment);
                if (fragment.getClass() == MapsFragment.class) {
                    MapsFragment mapsFragment = (MapsFragment) fragment;
                    autocompleteViewModel.fetchAutocompletePrediction(s, mapsFragment.getRectangularBounds());
                }
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
    public void onClickedMarker(String id) {
        this.showRestaurantDetailActivity(id);
    }

    /**
     * Manage click on Your Lunch button.
     */
    private void onClickYourLunch() {
        if (userDBViewModel.getUser().get() != null
                && userDBViewModel.getUser().get().getBookedRestaurantId() != null)
            this.showRestaurantDetailActivity(userDBViewModel.getUser().get().getBookedRestaurantId());
        else Snackbar.make(drawerLayout, R.string.activity_main_message_no_booked_restaurant, Snackbar.LENGTH_SHORT).show();
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
        if (this.getCurrentUser() != null) {
            userDBViewModel = new UserDBViewModel(this, userAuthViewModel.getUid());
            userDBViewModel.createUserInFirestore(userAuthViewModel.getUid(), userAuthViewModel.getUserName(), userAuthViewModel.getUserPicture());
        }
    }

    /**
     * Load user data from firestore.
     */
    private void loadUserDataFromFirestore() {
        if (userDBViewModel == null)
            userDBViewModel = new UserDBViewModel(this, userAuthViewModel.getUid());
        userDBViewModel.fetchUser();
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
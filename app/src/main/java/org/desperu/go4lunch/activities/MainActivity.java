package org.desperu.go4lunch.activities;

import android.content.Intent;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityMainNavHeaderBinding;
import org.desperu.go4lunch.fragments.MapFragment;
import org.desperu.go4lunch.viewmodel.UserViewModel;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    // FOR DESIGN
    private DrawerLayout drawerLayout;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.activity_main_nav_view) NavigationView navigationView;
    @BindView(R.id.activity_main_nav_bottom) BottomNavigationView bottomNavigationView;

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
        this.configureAndShowFragment();
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    /**
     * Configure Drawer layout.
     */
    private void configureDrawerLayout() {
        this.drawerLayout = findViewById(R.id.activity_main_drawer_layout);
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

        // Enable Data binding for user info
        View headerView = navigationView.getHeaderView(0);
        ActivityMainNavHeaderBinding activityMainNavHeaderBinding = ActivityMainNavHeaderBinding.bind(headerView);
        UserViewModel userViewModel = new UserViewModel(getBaseContext());
        activityMainNavHeaderBinding.setUserViewModel(userViewModel);
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

        MapFragment fragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.activity_main_frame_layout);

        if (fragment == null) {
            fragment = MapFragment.newInstance();
//            Bundle bundle = new Bundle();
//            bundle.putInt(KEY_FRAGMENT, NOTIFICATION_FRAGMENT);
//            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_frame_layout, fragment)
                    .commit();
        }
    }

    // -----------------
    // METHODS OVERRIDE
    // -----------------

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
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_main_menu_search:
//                this.showSearchArticlesActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // -----------------
    // ACTIVITY
    // -----------------

    /**
     * Log out of current login, and start Login Activity.
     */
    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(this, LoginActivity.class));
        this.finish();
    }
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
}
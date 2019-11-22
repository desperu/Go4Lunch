package org.desperu.go4lunch.activities;

import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseActivity;


public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        BottomNavigationView.OnNavigationItemSelectedListener {

    // FOR DESIGN
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

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
    }

    // -----------------
    // CONFIGURATION
    // -----------------

    /**
     * Configure ToolBar.
     */
    protected void configureToolBar() {
        this.toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

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
        this.navigationView = findViewById(R.id.activity_main_nav_view);
        // Support status bar for KitKat.
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            navigationView.setPadding(0, 0, 0, 0);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // -----------------
    // METHODS OVERRIDE
    // -----------------

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.activity_main_menu_drawer_your_lunch:
//                this.showSearchArticlesActivity();
                break;
            case R.id.activity_main_menu_drawer_settings:
//                this.showNotificationsActivity();
                break;
            case R.id.activity_main_menu_drawer_log_out:
//                this.showAboutDialog();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.activity_main_menu_search:
//                this.showSearchArticlesActivity();
                return true;
            case R.id.activity_main_menu_bottom_map:
//                this.showNotificationsActivity();
                return true;
            case R.id.activity_main_menu_bottom_list:
//                this.showAboutDialog();
                return true;
            case R.id.activity_main_menu_bottom_workmates:
//                this.showHelpDocumentation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
}
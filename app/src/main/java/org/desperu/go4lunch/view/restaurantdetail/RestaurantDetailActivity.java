package org.desperu.go4lunch.view.restaurantdetail;

import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityRestaurantDetailBinding;
import org.desperu.go4lunch.viewmodel.RestaurantViewModel;

import butterknife.BindView;

public class RestaurantDetailActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {

    //FOR DESIGN
    @BindView(R.id.activity_restaurant_detail_swipe_container) SwipeRefreshLayout swipeRefreshLayout;

    // FOR DATA
    public static final String RESTAURANT_ID = "restaurant id";

    private RestaurantViewModel restaurantViewModel;

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
        ActivityRestaurantDetailBinding restaurantDetailBinding = DataBindingUtil.setContentView(this, this.getActivityLayout());
        restaurantViewModel = new RestaurantViewModel(this, this.getIdFromIntentData());
        restaurantDetailBinding.setRestaurantViewModel(restaurantViewModel);
    }

    /**
     * Configure swipe refresh layout.
     */
    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onRefresh() {
        Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                restaurantViewModel.restartRequest();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }

    // --------------
    // ACTION
    // --------------

    // --------------
    // UI
    // --------------

    /**
     * Stop swipe refresh animation when received response request.
     */
    public void hideSwipeRefresh() { this.swipeRefreshLayout.setRefreshing(false); }
}
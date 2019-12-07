package org.desperu.go4lunch.view.restaurantdetail;

import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseActivity;
import org.desperu.go4lunch.databinding.ActivityRestaurantDetailBinding;
import org.desperu.go4lunch.viewmodel.RestaurantViewModel;

import butterknife.BindView;

public class RestaurantDetailActivity extends BaseActivity {

    //FOR DESIGN
    @BindView(R.id.activity_restaurant_detail_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;

    // FOR DATA
    public static final String RESTAURANT_ID = "restaurant id";

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getActivityLayout() { return R.layout.activity_restaurant_detail; }

    @Override
    protected void configureDesign() {
        this.configureDataBinding();
//        this.configureSwipeRefreshLayout();
    }

    // --------------
    // METHODS OVERRIDE
    // --------------

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Get restaurant id from intent data.
     * @return The restaurant id.
     */
    private String getIdFromIntentData() { return getIntent().getStringExtra(RESTAURANT_ID); }

    /**
     * Configure data binding.
     */
    private void configureDataBinding() {
        ActivityRestaurantDetailBinding restaurantDetailBinding = DataBindingUtil.setContentView(this, R.layout.activity_restaurant_detail);
        RestaurantViewModel restaurantViewModel = new RestaurantViewModel(this, this.getIdFromIntentData());
        restaurantDetailBinding.setRestaurantViewModel(restaurantViewModel);
    }

    /**
     * Configure swipe refresh layout.
     */
    private void configureSwipeRefreshLayout() {
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(this::configureDataBinding);
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
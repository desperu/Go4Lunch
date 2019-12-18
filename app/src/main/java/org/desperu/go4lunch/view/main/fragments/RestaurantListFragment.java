package org.desperu.go4lunch.view.main.fragments;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.libraries.places.api.model.Place;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.view.adapter.RestaurantListAdapter;
import org.desperu.go4lunch.viewmodel.NearbyPlaceViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class RestaurantListFragment extends BaseFragment {

    @BindView(R.id.fragment_recycler_view_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycler_view) RecyclerView recyclerView;

    private RestaurantListAdapter adapter;
    private List<RestaurantInfoViewModel> restaurantList = new ArrayList<>();

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_recycler_view; }

    @Override
    protected void configureDesign() {
        this.configureRecyclerView();
        this.loadNearbyRestaurantList();
        this.configureSwipeRefresh();
    }


    public RestaurantListFragment() {
        // Needed empty constructor
    }

    public static RestaurantListFragment newInstance() { return new RestaurantListFragment(); }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Configure recycler view.
     */
    private void configureRecyclerView() {
        // Create adapter passing in the sample user data
        this.adapter = new RestaurantListAdapter(R.layout.fragment_restaurant_list_item, restaurantList);
        // Attach the adapter to the recyclerView to populate items
        this.recyclerView.setAdapter(this.adapter);
        // Set layout manager to position the items
        this.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    /**
     * Load nearby restaurant list.
     */
    private void loadNearbyRestaurantList() {
        NearbyPlaceViewModel nearbyPlaceViewModel = new NearbyPlaceViewModel(this);
        nearbyPlaceViewModel.fetchNearbyRestaurant();
    }

    /**
     * Configure swipe to refresh.
     */
    private void configureSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadNearbyRestaurantList);
    }

    // --------------
    // UI
    // --------------

//    /**
//     * Update recycler view when received data.
//     * @param placeList List of nearby places.
//     */
//    public void updateRecyclerView(@NotNull List<Place> placeList) {
//        restaurantList.clear();
//        for (Place place : placeList)
//            restaurantList.add(new RestaurantInfoViewModel(getContext(), place.getId()));
//        adapter.notifyDataSetChanged();
//        swipeRefreshLayout.setRefreshing(false);
//    }

    /**
     * Update recycler view when received data.
     * @param placeIdList List of nearby places id.
     */
    public void updateRecyclerView(@NotNull List<String> placeIdList) {
        restaurantList.clear();
        for (String placeId : placeIdList)
            restaurantList.add(new RestaurantInfoViewModel(getContext(), placeId));
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}
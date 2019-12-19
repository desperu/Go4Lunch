package org.desperu.go4lunch.view.main.fragments;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.material.snackbar.Snackbar;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.adapter.RestaurantListAdapter;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.viewmodel.AutocompleteViewModel;
import org.desperu.go4lunch.viewmodel.NearbyPlaceViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import butterknife.BindView;

public class RestaurantListFragment extends BaseFragment {

    // FOR DESIGN
    @BindView(R.id.fragment_recycler_view_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycler_view) RecyclerView recyclerView;

    // FOR BUNDLE
    public static final String PLACE_ID_LIST_RESTAURANT_LIST = "placeIdList";
    public static final String QUERY_TERM_LIST = "queryTerm";
    public static final String BOUNDS = "bounds";

    // FOR DATA
    private ArrayList<String> placeIdList;
    private RestaurantListAdapter adapter;
    private ArrayList<RestaurantInfoViewModel> restaurantList = new ArrayList<>();

    // CALLBACK
    public interface RestaurantListFragmentListener {
        void onNewPlaceIdList(ArrayList<String> placeIdList);
    }

    private RestaurantListFragment.RestaurantListFragmentListener mCallback;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_recycler_view; }

    @Override
    protected void configureDesign() {
        this.createCallbackToParentActivity();
        this.setPlaceIdListFromBundle();
        this.configureRecyclerView();
        this.configureSwipeRefresh();
        this.updateRecyclerView();
    }


    public RestaurantListFragment() {
        // Needed empty constructor
    }

    public static RestaurantListFragment newInstance() { return new RestaurantListFragment(); }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Set place Id list from bundle.
     */
    private void setPlaceIdListFromBundle() {
        assert getArguments() != null;
        this.placeIdList = getArguments().getStringArrayList(PLACE_ID_LIST_RESTAURANT_LIST);
    }

    /**
     * Get query term from bundle.
     * @return Query term.
     */
    @Nullable
    private String getQueryTerm() {
        return getArguments() != null ? getArguments().getString(QUERY_TERM_LIST) : null;
    }

    /**
     * Get rectangular bounds from bundle.
     * @return Rectangular bounds.
     */
    @Nullable
    private RectangularBounds getBounds() {
        return getArguments() != null ? getArguments().getParcelable(BOUNDS): null;
    }

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
     * Configure swipe to refresh.
     */
    private void configureSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::reloadRestaurantList);
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    /**
     * Configure callback for parent activity to manage click on marker.
     */
    private void createCallbackToParentActivity(){
        try {
            mCallback = (RestaurantListFragmentListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement RestaurantListFragmentListener");
        }
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Reload restaurant list.
     */
    private void reloadRestaurantList() {
        if (getQueryTerm() != null && !getQueryTerm().isEmpty()) { // TODO a pb when refresh search
            AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(this);
            autocompleteViewModel.fetchAutocompletePrediction(getQueryTerm(), this.getBounds());
        } else {
            NearbyPlaceViewModel nearbyPlaceViewModel = new NearbyPlaceViewModel(this);
            nearbyPlaceViewModel.fetchNearbyRestaurant();
        }
    }

    // --------------
    // UI
    // --------------

    /**
     * Update recycler view with data (from maps fragment).
     */
    private void updateRecyclerView() {
        if (placeIdList != null && !placeIdList.isEmpty())
            this.updateRecyclerViewWithData(this.placeIdList);
    }

    /**
     * Update recycler view when received data only if query term isn't empty (from autocomplete request).
     * @param placeIdList List of found places id.
     */
    public void updateRecyclerViewWithAutocomplete(@NotNull ArrayList<String> placeIdList) {
        if (getQueryTerm() != null && !getQueryTerm().isEmpty())
            this.updateRecyclerViewWithData(placeIdList);
        else this.updateRecyclerView();
    }

    /**
     * Update recycler view when received data (from nearby restaurant request).
     * @param placeIdList List nearby restaurant id.
     */
    public void updateRecyclerViewWithNearbyRestaurants(ArrayList<String> placeIdList) {
        this.updateRecyclerViewWithData(placeIdList);
        Snackbar.make(swipeRefreshLayout, R.string.fragment_restaurant_list_snackbar_refresh_nearby_restaurant, Snackbar.LENGTH_SHORT).show();
    }

    // --------------
    // UTILS
    // --------------

    /**
     * Update recycler view with data.
     * @param placeIdList List of restaurant id.
     */
    private void updateRecyclerViewWithData(ArrayList<String> placeIdList) {
        mCallback.onNewPlaceIdList(placeIdList);
        restaurantList.clear();
        for (String placeId : placeIdList)
            restaurantList.add(new RestaurantInfoViewModel(getContext(), placeId));
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}
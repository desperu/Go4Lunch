package org.desperu.go4lunch.view.main.fragments;

import android.location.Location;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.material.snackbar.Snackbar;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.desperu.go4lunch.utils.ItemClickSupport;
import org.desperu.go4lunch.view.adapter.RestaurantListAdapter;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.viewmodel.AutocompleteViewModel;
import org.desperu.go4lunch.viewmodel.NearbyPlaceViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantDBViewModel;
import org.desperu.go4lunch.viewmodel.RestaurantInfoViewModel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;

public class RestaurantListFragment extends BaseFragment {

    // FOR DESIGN
    @BindView(R.id.fragment_recycler_view_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragment_recycler_view) RecyclerView recyclerView;

    // FOR BUNDLE
    public static final String PLACE_ID_LIST_RESTAURANT_LIST = "placesIdList";
    public static final String BOUNDS = "bounds";
    public static final String QUERY_TERM_LIST = "queryTerm";
    public static final String IS_QUERY_FOR_RESTAURANT_LIST = "isQueryForRestaurant";
    public static final String NEARBY_BOUNDS = "nearbyBounds";
    public static final String USER_LOCATION = "userLocation";

    // FOR DATA
    private ArrayList<String> placesIdList;
    private String queryTerm;
    private RectangularBounds bounds;
    private RestaurantListAdapter adapter;
    private ArrayList<RestaurantInfoViewModel> restaurantInfoList = new ArrayList<>();
    private ArrayList<RestaurantDBViewModel> restaurantDBList = new ArrayList<>();
    private ArrayList<Integer> placeDistanceList = new ArrayList<>();

    // CALLBACKS
    public interface OnNewDataListener {
        void onNewPlacesIdList(ArrayList<String> placeIdList);
        void onNewBounds(RectangularBounds bounds);
        void onNewQuerySearch(boolean isQueryForRestaurant);
    }

    public interface OnClickListener {
        void onItemClick(String restaurantId);
    }

    private OnNewDataListener dataCallback;

    private OnClickListener clickCallback;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_recycler_view; }

    @Override
    protected void configureDesign() {
        this.createDataCallbackToParentActivity();
        this.createClickCallbackToParentActivity();
        this.setDataFromBundle();
        this.configureRecyclerView();
        this.configureOnClickRecyclerViewItem();
        this.updateRecyclerViewWithData();
        this.configureSwipeRefresh();
    }


    public RestaurantListFragment() {
        // Needed empty constructor
    }

    @NotNull
    @Contract(" -> new")
    public static RestaurantListFragment newInstance() { return new RestaurantListFragment(); }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Set data from bundle, placesIdList, bounds and query term.
     */
    private void setDataFromBundle() {
        this.placesIdList = getArguments() != null ? getArguments().getStringArrayList(PLACE_ID_LIST_RESTAURANT_LIST) : null;
        this.bounds = getArguments() != null ? getArguments().getParcelable(BOUNDS): null;
        this.queryTerm = getArguments() != null ? getArguments().getString(QUERY_TERM_LIST) : null;
    }

    /**
     * Get query term last use, for restaurant search or not.
     * @return Query term last use.
     */
    private boolean getIsQueryForRestaurant() {
        assert getArguments() != null;
        return getArguments().getBoolean(IS_QUERY_FOR_RESTAURANT_LIST);
    }

    /**
     * Get nearby rectangular bounds from bundle.
     * @return Nearby rectangular bounds.
     */
    @Nullable
    private RectangularBounds getNearbyBounds() {
        return getArguments() != null ? getArguments().getParcelable(NEARBY_BOUNDS) : null;
    }

    /**
     * Get user location from bundle.
     * @return User location.
     */
    @Nullable
    private Location getUserLocation() {
        return getArguments() != null ? getArguments().getParcelable(USER_LOCATION) : null;
    }

    /**
     * Configure recycler view.
     */
    private void configureRecyclerView() {
        // Create adapter passing in the sample user data
        this.adapter = new RestaurantListAdapter(R.layout.fragment_restaurant_list_item, restaurantInfoList, restaurantDBList);
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
     * Configure data callback for parent activity to return new restaurant list.
     */
    private void createDataCallbackToParentActivity(){
        try {
            dataCallback = (OnNewDataListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnNewDataListener");
        }
    }

    /**
     * Configure click callback for parent activity to manage click.
     */
    private void createClickCallbackToParentActivity(){
        try {
            clickCallback = (OnClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnClickListener");
        }
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Reload corresponding restaurant list.
     */
    private void reloadRestaurantList() {
        if (queryTerm != null && !queryTerm.isEmpty())
            this.getAutocompleteRestaurant(queryTerm);
        else this.getNearbyRestaurant();
    }

    /**
     * Get nearby restaurant list.
     */
    private void getNearbyRestaurant() {
        // Start request
        NearbyPlaceViewModel nearbyPlaceViewModel = new NearbyPlaceViewModel(Objects.requireNonNull(getActivity()).getApplication());
        nearbyPlaceViewModel.fetchNearbyRestaurant();
        // Get request result
        nearbyPlaceViewModel.getPlacesList().observe(this, placesList -> {
            ArrayList<String> placesIdList = new ArrayList<>();
            for (Place place : placesList)
                placesIdList.add(place.getId());
            this.updateRecyclerView(placesIdList);
            this.placesIdList = placesIdList;
        });
        dataCallback.onNewBounds(this.bounds = this.getNearbyBounds());
        if (swipeRefreshLayout.isShown())
            Snackbar.make(swipeRefreshLayout, R.string.fragment_restaurant_list_snackbar_refresh_nearby_restaurant, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Get autocomplete search result restaurant list.
     * @param query Query term.
     */
    private void getAutocompleteRestaurant(String query) {
        dataCallback.onNewQuerySearch(true);
        // Start request
        assert getActivity() != null;
        AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(getActivity().getApplication());
        autocompleteViewModel.fetchAutocompletePrediction(query, this.bounds);
        // Get request result
        autocompleteViewModel.getPlacesIdListLiveData().observe(this, this::updateRecyclerView);

    }

    // --------------
    // ACTION
    // --------------

    /**
     * Configure click on recycler view item.
     */
    private void configureOnClickRecyclerViewItem() {
        ItemClickSupport.addTo(recyclerView, R.layout.fragment_restaurant_list_item)
                .setOnItemClickListener((recyclerView, position, v) -> {
                    if (adapter.getRestaurantInfo(position).getPlace().get() != null)
                        clickCallback.onItemClick(Objects.requireNonNull(
                                adapter.getRestaurantInfo(position).getPlace().get()).getId());
                });
    }

    /**
     * Method called when query text change.
     * @param query Query term.
     */
    public void onSearchQueryTextChange(String query) {
        this.queryTerm = query;
        this.reloadRestaurantList();
    }

    // --------------
    // UI
    // --------------

    /**
     * Update recycler view with corresponding data, from maps fragment or search with query term.
     */
    private void updateRecyclerViewWithData() {
        if (this.placesIdList != null && !this.placesIdList.isEmpty() && this.getIsQueryForRestaurant()
                || placesIdList == null || placesIdList.isEmpty())
            this.updateRecyclerView(this.placesIdList);
        else reloadRestaurantList();
    }

    /**
     * Update recycler view with sort list, by distance.
     * @param placeIdList List of restaurant id.
     */
    private void updateRecyclerView(ArrayList<String> placeIdList) {
        dataCallback.onNewPlacesIdList(placeIdList);
        this.setRestaurantInfoAndDBList(placeIdList);
        this.setRestaurantDistanceList();
        if (placeIdList.isEmpty()) this.sortRestaurantByDistance();
        swipeRefreshLayout.setRefreshing(false);
    }

    // --------------
    // UTILS
    // --------------

    /**
     * Set restaurant info and DB list from place id list.
     * @param placeIdList List of place id.
     */
    private void setRestaurantInfoAndDBList(@NotNull ArrayList<String> placeIdList) {
        assert getActivity() != null;
        assert this.getUserLocation() != null;
        this.restaurantInfoList.clear();
        this.restaurantDBList.clear();
        for (String placeId : placeIdList) {
            // Google place info
            RestaurantInfoViewModel restaurantInfoViewModel =
                    new RestaurantInfoViewModel(getActivity().getApplication(), placeId);
            restaurantInfoViewModel.setLocationData(new LatLng(this.getUserLocation().getLatitude(), getUserLocation().getLongitude()));
            this.restaurantInfoList.add(restaurantInfoViewModel);

            // Restaurant data base
            RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(getActivity().getApplication(), placeId);
            restaurantDBViewModel.fetchRestaurant();
            this.restaurantDBList.add(restaurantDBViewModel);
        }
    }

    /**
     * Set place distance list.
     */
    private void setRestaurantDistanceList() {
        assert getUserLocation() != null;
        placeDistanceList.clear();
        for (RestaurantInfoViewModel restaurantInfoViewModel : restaurantInfoList) {
            restaurantInfoViewModel.getPlaceLiveData().observe(this, place -> {
                placeDistanceList.add(Integer.parseInt(
                        Go4LunchUtils.getRestaurantDistance(getContext(),
                        new LatLng(this.getUserLocation().getLatitude(),
                        getUserLocation().getLongitude()), place.getLatLng()).replace("m", "")));
                if (restaurantInfoList.size() == placeDistanceList.size())
                    this.sortRestaurantByDistance();
            });
        }
    }

    /**
     * Sort restaurant info and DB list by distance from user position.
     */
    private void sortRestaurantByDistance() {
        for (int i = 0; i < placeDistanceList.size(); i++) {
            for (int j = 0; j < placeDistanceList.size(); j++) {
                // Compare distances to sort minus to upper
                if (placeDistanceList.get(j) < placeDistanceList.get(i) && j >= i) {
                    restaurantInfoList.add(i, restaurantInfoList.get(j));
                    restaurantInfoList.remove(j + 1);
                    restaurantDBList.add(i, restaurantDBList.get(j));
                    restaurantDBList.remove(j + 1);
                    placeDistanceList.add(i, placeDistanceList.get(j));
                    placeDistanceList.remove(j + 1);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}
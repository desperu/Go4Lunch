package org.desperu.go4lunch.view.main.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.utils.Go4LunchPrefs;
import org.desperu.go4lunch.utils.MarkerUtils;
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
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.*;
import static org.desperu.go4lunch.Go4LunchTools.PrefsKeys.*;
import static org.desperu.go4lunch.Go4LunchTools.SettingsDefault.*;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener {

    // FOR DESIGN
    @BindView(R.id.map) MapView mapView;

    // FOR BUNDLE
    public static final String QUERY_TERM_MAPS = "queryTerm";
    public static final String PLACE_ID_LIST_MAPS = "placeIdList";
    public static final String CAMERA_POSITION = "cameraPosition";

    // FOR DATA
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private String queryTerm;
    private boolean isPlacesUpdating = false;

    // FOR LOCATION
    private boolean isLocationEnabled = false;
    private Location myLocation;
    // FOR LOCATION UPDATES
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static LocationRequest locationRequest = null;

    // CALLBACKS
    public interface OnNewDataOrClickListener {
        void onClickedMarker(String id);
        void saveNearbyBounds(RectangularBounds nearbyBounds);
        void onNewUserLocation(Location userLocation);
        void onNewPlacesIdList(ArrayList<String> placesIdList);
        void onNewBounds(RectangularBounds bounds);
        void onNewCameraPosition(CameraPosition cameraPosition);
    }

    private OnNewDataOrClickListener mCallback;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_maps; }

    @Override
    protected void configureDesign() {
        this.configureMapFragment();
        this.createCallbackToParentActivity();
        this.setQueryTermFromBundle();
    }


    public MapsFragment() {
        // Needed empty constructor
    }

    @NotNull
    @Contract(" -> new")
    public static MapsFragment newInstance() { return new MapsFragment(); }

    // --------------
    // METHODS OVERRIDE
    // --------------

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        // Check location permission, enable MyLocation,
        // and hide google MyLocation button to use custom.
        this.checkLocationPermissionsStatus();
        mMap.setMyLocationEnabled(isLocationEnabled);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (isLocationEnabled) this.updateMapWithLocation(this.getUserLocation());
        boolean isRefreshLocationEnabled = Go4LunchPrefs.getBoolean(getContext(), MAP_AUTO_REFRESH_LOCATION, AUTO_REFRESH_DEFAULT);
        if (isLocationEnabled && fusedLocationClient == null && isRefreshLocationEnabled) this.startLocationUpdates();
        else if (!isRefreshLocationEnabled) this.stopLocationUpdates();

        // Show zoom control, and reposition them.
        mMap.getUiSettings().setZoomControlsEnabled(Go4LunchPrefs.getBoolean(getContext(), MAP_ZOOM_BUTTON, ZOOM_BUTTON_DEFAULT));
        this.repositionMapButton(GOOGLE_MAP_ZOOM_OUT_BUTTON, (int) getResources().getDimension(R.dimen.fragment_maps_zoom_button_margin_bottom), (int) getResources().getDimension(R.dimen.fragment_maps_zoom_button_margin_end));

        // Set gestures for google map.
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);

        // Set onMarkerClick Listener
        mMap.setOnMarkerClickListener(this);

        // Set map style
        assert getContext() != null;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        // Restore map state, or update map with restaurant
        if (!this.restoreState()) this.startNewRequest(queryTerm);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        this.isLocationEnabled = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (isLocationEnabled) this.updateMapWithLocation(this.getUserLocation());
    }

    @Override
    public boolean onMarkerClick(@NotNull Marker marker) {
        this.repositionMapButton(GOOGLE_MAP_TOOLBAR, (int) getResources().getDimension(R.dimen.fragment_maps_toolbar_margin_bottom), (int) getResources().getDimension(R.dimen.fragment_maps_toolbar_margin_end));
        if (marker.getSnippet() != null)
            mCallback.onClickedMarker(marker.getSnippet());
        else Toast.makeText(getContext(), R.string.fragment_maps_no_place_found, Toast.LENGTH_SHORT).show();
        return true; // TODO false to show windows info and to navigate
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        mMap.addMarker(new MarkerOptions().position(latLng));
    }

    @Override
    public void onCameraIdle() {
        mCallback.onNewCameraPosition(mMap.getCameraPosition());
        mCallback.onNewBounds(getRectangularBounds());
        assert getActivity() != null;
        if (queryTerm != null && !isPlacesUpdating) // TODO how don't launch
            this.getAutocompleteRestaurant(queryTerm);
    }

    @Override
    public void onPause() {
        super.onPause();
        this.stopLocationUpdates();
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    /**
     * Configure callback for parent activity to manage click on marker.
     */
    private void createCallbackToParentActivity(){
        try {
            mCallback = (OnNewDataOrClickListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnNewDataOrClickListener");
        }
    }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Configure and show map fragment.
     */
    private void configureMapFragment() {
        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            mapFragment.getMapAsync(this);
        }
        getChildFragmentManager().beginTransaction()
                .add(R.id.map, mapFragment)
                .commit();
    }

    /**
     * Restore last map state (position and restaurants).
     * @return If a previous state is restored.
     */
    private boolean restoreState() {
        if (this.getPlaceIdList() != null && this.getCameraPosition() != null
                && this.queryTerm != null && !this.queryTerm.isEmpty()) {
            this.isPlacesUpdating = true;
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(this.getCameraPosition()));
            for (String restaurantId : this.getPlaceIdList())
                this.getRestaurantInfo(restaurantId);
            return true;
        }
        return false;
    }

    /**
     * Check if Coarse Location and Fine Location are granted, if not, ask for them.
     */
    private void checkLocationPermissionsStatus() {
        assert getContext() != null;
        if (!EasyPermissions.hasPermissions(getContext(), PERMS))
            EasyPermissions.requestPermissions(this, getString(R.string.fragment_maps_popup_title_permission_location),
                    PERM_COARSE_LOCATION, PERMS);
        else isLocationEnabled = true;
    }

    /**
     * Set query term from bundle argument.
     */
    private void setQueryTermFromBundle() {
        this.queryTerm = getArguments() != null ? getArguments().getString(QUERY_TERM_MAPS) : null;
    }

    /**
     * Get place id list from bundle.
     * @return List of place id.
     */
    @Nullable
    private ArrayList<String> getPlaceIdList() {
        return getArguments() != null ? getArguments().getStringArrayList(PLACE_ID_LIST_MAPS) : null;
    }

    /**
     * Get last camera position from bundle.
     * @return Camera position.
     */
    @Nullable
    private CameraPosition getCameraPosition() {
        return getArguments() != null ? getArguments().getParcelable(CAMERA_POSITION) : null;
    }

    // --------------
    // ACTION
    // --------------

    @OnClick(R.id.fragment_maps_floating_button_location)
    void myLocationButtonListener() {
        mMap.setMyLocationEnabled(isLocationEnabled);
        if (isLocationEnabled) this.updateMapWithLocation(this.getUserLocation());
        else this.checkLocationPermissionsStatus();
        this.startNewRequest(this.queryTerm);
    }

    /**
     * Method called when query text change.
     * @param query Query term.
     */
    public void onSearchQueryTextChange(@NotNull String query) {
        this.queryTerm = query;
        this.startNewRequest(query);
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Start new request to get restaurant.
     * @param query Query term.
     */
    private void startNewRequest(String query) {
        this.isPlacesUpdating = true;
        if (mMap != null) mMap.clear();
        if (query != null && !query.isEmpty())
            this.getAutocompleteRestaurant(query);
        else this.getNearbyRestaurant();
    }

    /**
     * Get nearby restaurants with place api.
     */
    private void getNearbyRestaurant() {
        // Start nearby request
        assert getActivity() != null;
        NearbyPlaceViewModel nearbyPlaceViewModel = new NearbyPlaceViewModel(getActivity().getApplication());
        nearbyPlaceViewModel.fetchNearbyRestaurant();

        // Get nearby request result
        nearbyPlaceViewModel.getPlacesList().observe(this, placesList -> {
            ArrayList<String> placesIdList = new ArrayList<>();
            for (Place place : placesList) {
                this.getRestaurantDB(place);
                placesIdList.add(place.getId());
            }
            mCallback.onNewPlacesIdList(placesIdList);
            mCallback.saveNearbyBounds(this.getRectangularBounds()); // TODO correct, put in location listener method, problem when play between custom map position en fragment,
        });
    }

    /**
     * Get autocomplete restaurant response.
     * @param query Query term.
     */
    private void getAutocompleteRestaurant(@NotNull String query) {
        // Start autocomplete request
        assert getActivity() != null;
        AutocompleteViewModel autocompleteViewModel = new AutocompleteViewModel(getActivity().getApplication());
        autocompleteViewModel.fetchAutocompletePrediction(query, getRectangularBounds());
        // Get autocomplete request result
        autocompleteViewModel.getPlacesIdListLiveData().observe(this, placesIdList -> {
            for (String restaurantId : placesIdList)
                this.getRestaurantInfo(restaurantId);
            mCallback.onNewPlacesIdList(placesIdList);
        });
    }

    /**
     * Get restaurant info from places api.
     * @param restaurantId Restaurant id.
     */
    private void getRestaurantInfo(String restaurantId) {
        // Start place info request
        assert getActivity() != null;
        RestaurantInfoViewModel restaurantInfoViewModel =
                new RestaurantInfoViewModel(getActivity().getApplication(), restaurantId);
        // Get place info request result
        restaurantInfoViewModel.getPlaceLiveData().observe(this, this::getRestaurantDB);
    }

    /**
     * Get restaurant DB from firestore.
     * @param place Restaurant place object.
     */
    private void getRestaurantDB(@NotNull Place place) {
        // Start DB request
        RestaurantDBViewModel restaurantDBViewModel = new RestaurantDBViewModel(place.getId());
        restaurantDBViewModel.fetchRestaurant();
        // Get DB request result
        restaurantDBViewModel.getRestaurantLiveData().observe(this, restaurant ->
                this.addMarker(restaurant, place));
    }

    // --------------
    // UI
    // --------------

    /**
     * Update map with user location, animate camera to this point.
     * @param userLocation Current user location.
     */
    private void updateMapWithLocation(Location userLocation) {
        if (userLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(userLocation.getLatitude(), userLocation.getLongitude()),
                    Go4LunchPrefs.getInt(getContext(), MAP_ZOOM_LEVEL, ZOOM_LEVEL_DEFAULT)),
                    1500, null);
            this.myLocation = userLocation;
        }
    }

    /**
     * Add a new marker on the map.
     * @param restaurant Restaurant object with data from firestore.
     * @param place Current place object found.
     */
    private void addMarker(Restaurant restaurant, @NotNull Place place) {
        mMap.addMarker(new MarkerOptions()
                .position(Objects.requireNonNull(place.getLatLng()))
                .title(place.getName())
                .icon(this.switchMarkerColors(this.isBookedRestaurant(restaurant)))
                .snippet(place.getId()));
        this.isPlacesUpdating = false;
    }

    /**
     * Position map button correctly (toolbar and zoom) with regard to custom My Location button.
     * @param buttonTag Tag of the button.
     * @param marginBottom Margin bottom value for button.
     * @param marginEnd Margin end value for button.
     */
    private void repositionMapButton(String buttonTag, int marginBottom, int marginEnd) {
        if (mapView != null && mapView.findViewWithTag(buttonTag) != null) {
            // Get the toolbar or zoom button view
            View button = mapView.findViewWithTag(buttonTag);
            if (buttonTag.equals(GOOGLE_MAP_TOOLBAR)) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                        button.getLayoutParams();
                // position to the left of custom My Location button
                layoutParams.setMargins(0, 0, 0, marginBottom);
                layoutParams.setMarginEnd(marginEnd);
                button.setBottom(marginBottom);
            } else if (buttonTag.equals(GOOGLE_MAP_ZOOM_OUT_BUTTON)) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)
                        button.getLayoutParams();
                // position to the top of custom My Location button
                layoutParams.setMargins(0, 0, marginEnd, marginBottom);
            }
        }
    }

    // --------------
    // UTILS
    // --------------

    /**
     * Check if restaurant is booked.
     * @param restaurant Restaurant object.
     * @return Boolean value if is booked.
     */
    @Contract("null -> false")
    private boolean isBookedRestaurant(Restaurant restaurant) {
        if (restaurant != null) return !restaurant.getBookedUsersId().isEmpty();
        else return false;
    }

    /**
     * Switch marker color, if booked or not.
     * @param isBooked Is restaurant booked.
     * @return Marker bitmap with corresponding color.
     */
    @NotNull
    private BitmapDescriptor switchMarkerColors(boolean isBooked) {
        assert getContext() != null;
        if (isBooked)
            return BitmapDescriptorFactory.fromBitmap(MarkerUtils.createBitmapFromView(getContext(),
                    R.layout.custom_marker_layout, getResources().getColor(R.color.colorMarkerBookedFont),
                    getResources().getColor(R.color.colorMarkerBookedCutlery)));
        else
            return BitmapDescriptorFactory.fromBitmap(MarkerUtils.createBitmapFromView(getContext(),
                    R.layout.custom_marker_layout, getResources().getColor(R.color.colorMarkerNotBookedFont),
                    getResources().getColor(R.color.colorMarkerNotBookedCutlery)));
    }

    // --------------
    // LOCATION
    // --------------

    /**
     * Get current location, or last know.
     * @return User location.
     */
    @SuppressLint("MissingPermission")
    private Location getUserLocation() {
        assert getContext() != null;
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        assert lm != null;
        myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            assert provider != null;
            myLocation = lm.getLastKnownLocation(provider);
        }
        mCallback.onNewUserLocation(myLocation);
        return myLocation;
    }

    /**
     * Get screen global location.
     * @return Rectangular bounds for current screen.
     */
    @NotNull
    private RectangularBounds getRectangularBounds() {
        return RectangularBounds.newInstance(mMap.getProjection().getVisibleRegion().latLngBounds);
    }


    /**
     * Start location updates.
     */
    private void startLocationUpdates() {
        assert getContext() != null;
        fusedLocationClient = getFusedLocationProviderClient(getContext());

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(50_000L);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                mCallback.onNewUserLocation(locationResult.getLastLocation());
                if (myLocation != null && myLocation.distanceTo(locationResult.getLastLocation()) > 10) {
                    startNewRequest(queryTerm);
                    updateMapWithLocation(locationResult.getLastLocation());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, null);
    }

    /**
     * Stop location updates.
     */
    private void stopLocationUpdates() {
        if (fusedLocationClient != null) {
            try {
                locationRequest = null;
                fusedLocationClient.flushLocations();
                final Task<Void> voidTask = fusedLocationClient.removeLocationUpdates(locationCallback);
//                locationCallback = null;
                if (voidTask.isSuccessful()) {
                    Log.d(getClass().getSimpleName(),"StopLocation updates successful! ");
                } else {
                    Log.d(getClass().getSimpleName(),"StopLocation updates unsuccessful! " + voidTask.toString());
                }
            }
            catch (SecurityException exp) {
                Log.d(getClass().getSimpleName(), " Security exception while removeLocationUpdates");
            }
        }
    }
}
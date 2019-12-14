package org.desperu.go4lunch.view.main.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.RectangularBounds;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.base.BaseFragment;
import org.desperu.go4lunch.databinding.FragmentMapsBinding;
import org.desperu.go4lunch.utils.MarkerUtils;
import org.desperu.go4lunch.viewmodel.PlaceViewModel;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.*;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    // FOR DESIGN
    @BindView(R.id.map) MapView mapView;

    // FOR DATA
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private boolean isLocationEnabled = false;

    // CALLBACK
    public interface OnMarkerClickedListener {
        void onClickedMarker(String id);
    }

    private MapsFragment.OnMarkerClickedListener mCallback;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_maps; }

    @Override
    protected void configureDesign() {
        this.configureMapFragment();
        this.createCallbackToParentActivity();
    }


    public MapsFragment() {
        // Needed empty constructor
    }

    public static MapsFragment newInstance() { return new MapsFragment(); }

    // --------------
    // METHODS OVERRIDE
    // --------------

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        // Check location permission, enable MyLocation button,
        // and hide google MyLocation button to use custom.
        this.checkLocationPermissionsStatus();
        mMap.setMyLocationEnabled(isLocationEnabled);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (isLocationEnabled) this.setMapWithLocation();

        // Hide map toolbar.
//        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Configure data binding after google map is configured.
        this.configureDataBindingMapsFragment();

        // Show zoom control, and reposition them.
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // TODO get from shared pref on/off zoom
        this.repositionMapButton(GOOGLE_MAP_ZOOM_OUT_BUTTON, (int) getResources().getDimension(R.dimen.fragment_maps_zoom_button_margin_bottom), (int) getResources().getDimension(R.dimen.fragment_maps_zoom_button_margin_end));


        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMapLongClickListener(this);

        // Set onMarkerClick Listener
        mMap.setOnMarkerClickListener(this);

        // Set map style
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        // Update map with restaurant
        this.updateMapWithNearbyRestaurant();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        this.isLocationEnabled = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        this.repositionMapButton(GOOGLE_MAP_TOOLBAR, (int) getResources().getDimension(R.dimen.fragment_maps_toolbar_margin_bottom), (int) getResources().getDimension(R.dimen.fragment_maps_toolbar_margin_end));
        if (marker.getSnippet() != null)
            mCallback.onClickedMarker(marker.getSnippet());
        else Toast.makeText(getContext(), R.string.fragment_maps_no_place_found, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO Use fetch to get place name or remove?? create a bug
        mMap.addMarker(new MarkerOptions().position(latLng));
    }

    @Override
    public void onResume() {
        super.onResume();
        this.updateMapWithNearbyRestaurant();
    }

    // --------------
    // FRAGMENT SUPPORT
    // --------------

    /**
     * Configure callback for parent activity to manage click on marker.
     */
    private void createCallbackToParentActivity(){
        try {
            mCallback = (MapsFragment.OnMarkerClickedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e.toString()+ " must implement OnClickedMarkerListener");
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
     * Set map with current location, or last know.
     */
    @SuppressLint("MissingPermission")
    private void setMapWithLocation() {
        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }

        if (myLocation != null) {
            LatLng userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            // TODO enable disable zoom button with sharedPreferences
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18), 1500, null);
        }
    }

    /**
     * Check if Coarse Location and Fine Location are granted, if not, ask for them.
     */
    private void checkLocationPermissionsStatus() {
        if (!EasyPermissions.hasPermissions(getContext(), PERMS))
            EasyPermissions.requestPermissions(this, getString(R.string.fragment_maps_popup_title_permission_location),
                    PERM_COARSE_LOCATION, PERMS);
        else isLocationEnabled = true;
    }

    // TODO to perform
    /**
     * Configure data binding for the map view.
     */
    private void configureDataBindingMapsFragment() {
        FragmentMapsBinding fragmentMapsBinding = DataBindingUtil.bind(this.getFragmentView());//mapView.getRootView());
        PlaceViewModel placeViewModel = new PlaceViewModel(this);
        fragmentMapsBinding.setPlaceViewModel(placeViewModel);
    }

    // --------------
    // ACTION
    // --------------

    @OnClick(R.id.fragment_maps_floating_button_location)
    protected void myLocationButtonListener() {
        mMap.setMyLocationEnabled(isLocationEnabled);
        if (isLocationEnabled) this.setMapWithLocation();
        else this.checkLocationPermissionsStatus();
        this.updateMapWithNearbyRestaurant();
    }

    // --------------
    // UI
    // --------------

    /**
     * Update map with nearby restaurants.
     */
    private void updateMapWithNearbyRestaurant() {
//        mMap.setMyLocationEnabled(isLocationEnabled);
        if (mMap != null) mMap.clear();

        PlaceViewModel placeViewModel = new PlaceViewModel(this); // TODO use fields
        placeViewModel.getNearbyRestaurant();
    }

    /**
     * Add a new marker on the map.
     * @param latLng Latitude and longitude for the marker.
     * @param title Title for the marker.
     * @param restaurantId Restaurant place id.
     */
    public void addMarker(LatLng latLng, String title, String restaurantId) {
        mMap.addMarker(new MarkerOptions().position(latLng).title(title)
                .icon(this.switchMarkerColors(this.isBookedRestaurant())).snippet(restaurantId));
    }

    // TODO in RestaurantViewModel
    private boolean isBookedRestaurant() {
        return false;
    }

    /**
     * Switch marker color, if booked or not.
     * @param isBooked Is restaurant booked.
     * @return Marker bitmap with corresponding color.
     */
    private BitmapDescriptor switchMarkerColors(boolean isBooked) {
        Bitmap markerBitmap;

        if (isBooked)
            markerBitmap = MarkerUtils.createBitmapFromView(getContext(),
                    R.layout.custom_marker_layout, getResources().getColor(R.color.colorMarkerBookedFont),
                    getResources().getColor(R.color.colorMarkerBookedCutlery));

        else markerBitmap = MarkerUtils.createBitmapFromView(getContext(),
                R.layout.custom_marker_layout, getResources().getColor(R.color.colorMarkerNotBookedFont),
                getResources().getColor(R.color.colorMarkerNotBookedCutlery));

        return BitmapDescriptorFactory.fromBitmap(markerBitmap);
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
    // LOCATION
    // --------------

    /**
     * Get screen global location.
     * @return Rectangular bounds for current screen.
     */
    @NotNull
    private RectangularBounds getRectangularBounds() {
        return RectangularBounds.newInstance(mMap.getProjection().getVisibleRegion().latLngBounds);
    }
    // TODO add on map move Listener
}
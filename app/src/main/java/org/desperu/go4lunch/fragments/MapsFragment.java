package org.desperu.go4lunch.fragments;

import android.Manifest;
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

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseFragment;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.*;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

    // FOR DESIGN
    @BindView(R.id.map) MapView mapView;

    // FOR DATA
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;

    // FOR PERMISSION
    private static final String[] PERMS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    private static final int PERM_COARSE_LOCATION = 100;
    private boolean isLocationEnabled = false;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getFragmentLayout() { return R.layout.fragment_maps; }

    @Override
    protected void configureDesign() { this.configureMapFragment(); }


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
        LatLng latLng = new LatLng(1.289545, 103.849972);
        mMap.addMarker(new MarkerOptions().position(latLng)
                .title("Singapore"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        // Check location permission, enable MyLocation button,
        // and hide google MyLocation button to use custom.
        this.checkLocationPermissionsStatus();
        mMap.setMyLocationEnabled(isLocationEnabled);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (isLocationEnabled) this.setMapWithLocation();

        // Hide map toolbar.
//        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Show zoom control, and reposition them.
        mMap.getUiSettings().setZoomControlsEnabled(true);
        this.repositionMapButton(GOOGLE_MAP_ZOOM_OUT_BUTTON, ZOOM_OUT_MARGIN_BOTTOM, ZOOM_OUT_MARGIN_END);

        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMapLongClickListener(this);

        // Update map with restaurant
        this.updateMapWithNearbyRestaurant();

        // Set onMarkerClick Listener
        mMap.setOnMarkerClickListener(this);
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
        this.repositionMapButton(GOOGLE_MAP_TOOLBAR, TOOLBAR_MARGIN_BOTTOM, TOOLBAR_MARGIN_END);
        return false;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO Use fetch to get place name
        mMap.addMarker(new MarkerOptions().position(latLng));
    }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Configure and show map fragment.
     */
    private void configureMapFragment() {
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 18), 1500, null);
        }
    }

    /**
     * Check if Coarse Location and Fine Location are granted, if not, ask for them.
     */
    private void checkLocationPermissionsStatus() {
        if (!EasyPermissions.hasPermissions(getContext(), PERMS))
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_location),
                    PERM_COARSE_LOCATION, PERMS);
        else isLocationEnabled = true;
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
        mMap.setMyLocationEnabled(isLocationEnabled);
        mMap.clear();

        // TODO use ViewModel
        Places.initialize(getContext(), BuildConfig.google_maps_api_key);
        PlacesClient placesClient = Places.createClient(getContext());

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest findRequest = FindCurrentPlaceRequest.builder(placeFields).build();

        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(findRequest);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                FindCurrentPlaceResponse response = task.getResult();
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    Log.i("TAG", String.format("Place '%s' has likelihood: %f",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));

                    for (Place.Type type : placeLikelihood.getPlace().getTypes()) {
                        if ((type.toString().equals("RESTAURANT") || type.toString().equals("FOOD"))
                                && placeLikelihood.getPlace().getLatLng() != null)
                                mMap.addMarker(new MarkerOptions()
                                        .position(placeLikelihood.getPlace().getLatLng())
                                        .title(placeLikelihood.getPlace().getName()));
                    }
                }
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e("TAG", "Place not found: " + apiException.getStatusCode());
                }
            }
        });

//        FetchPlaceRequest request = FetchPlaceRequest.newInstance("restaurant", placeFields);
//        placesClient.fetchPlace(request);
//
//        placesClient.findCurrentPlace(findRequest);




//        var request = {
//                location: pyrmont,
//                type: ['restaurant']
//  };
//
//        infowindow = new google.maps.InfoWindow();
//        places = new google.maps.places.PlacesService(map);
//        places.nearbySearch(request, callback);
    }

    /**
     * Position map button correctly from custom My Location button.
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
            } else if (buttonTag.equals(GOOGLE_MAP_ZOOM_OUT_BUTTON)) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)
                        button.getLayoutParams();
                // position to the top of custom My Location button
                layoutParams.setMargins(0, 0, marginEnd, marginBottom);
            }
        }
    }
}
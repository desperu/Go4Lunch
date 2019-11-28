package org.desperu.go4lunch.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseFragment;

import butterknife.BindView;
import pub.devrel.easypermissions.EasyPermissions;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {

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
    protected int getFragmentLayout() { return R.layout.fragment_map; }

    @Override
    protected void configureDesign() { this.configureMapFragment(); }


    public MapFragment() {
        // Needed empty constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    // --------------
    // METHODS OVERRIDE
    // --------------


//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mapView.onCreate(savedInstanceState);
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        LatLng latLng = new LatLng(1.289545, 103.849972);
        mMap.addMarker(new MarkerOptions().position(latLng)
                .title("Singapore"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        // Enable MyLocation button
        this.checkLocationPermissionsStatus();
        mMap.setMyLocationEnabled(isLocationEnabled);
        mMap.getUiSettings().setMyLocationButtonEnabled(isLocationEnabled);
        if (isLocationEnabled) this.positionMyLocationButton();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) isLocationEnabled = true;
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

//    private void configureMapView() {
//        MapsInitializer.initialize(getActivity().getBaseContext());
//        mapView.getMapAsync(this);
//    }

    /**
     * Configure Google Places API.
     */
    private void configurePlaceAPI() {
        Places.initialize(getContext(), BuildConfig.google_maps_api_key);
        PlacesClient placesClient = Places.createClient(getContext());
    }

    /**
     * Check if Coarse Location and Fine Location are granted, if not, ask for them.
     */
    private void checkLocationPermissionsStatus() {
        if (!EasyPermissions.hasPermissions(getContext(), PERMS))
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_coarse_location),
                    PERM_COARSE_LOCATION, PERMS);
        else isLocationEnabled = true;
    }

    // --------------
    // UI
    // --------------

    /**
     * Position MyLocation button to bottom right corner.
     */
    private void positionMyLocationButton() {
        if (mapView != null && mapView.findViewWithTag("GoogleMapMyLocationButton") != null) {
            // Get the button view
            View locationButton = mapView.findViewWithTag("GoogleMapMyLocationButton");
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 30);
        }
    }
}
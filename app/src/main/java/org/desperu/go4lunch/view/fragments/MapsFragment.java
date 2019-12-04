package org.desperu.go4lunch.view.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.base.BaseFragment;
import org.desperu.go4lunch.databinding.FragmentMapsBinding;
import org.desperu.go4lunch.viewmodel.PlaceViewModel;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import pub.devrel.easypermissions.EasyPermissions;

import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.GOOGLE_MAP_TOOLBAR;
import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.GOOGLE_MAP_ZOOM_OUT_BUTTON;
import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.TOOLBAR_MARGIN_BOTTOM;
import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.TOOLBAR_MARGIN_END;
import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.ZOOM_OUT_MARGIN_BOTTOM;
import static org.desperu.go4lunch.Go4LunchTools.GoogleMap.ZOOM_OUT_MARGIN_END;

public class MapsFragment extends BaseFragment implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnMapLongClickListener {

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
    protected void configureDesign() {
        this.configureMapFragment();
//        this.configureDataBindingMapsFragment();
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
        this.repositionMapButton(GOOGLE_MAP_ZOOM_OUT_BUTTON, ZOOM_OUT_MARGIN_BOTTOM, ZOOM_OUT_MARGIN_END);

        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.setOnMapLongClickListener(this);

        // Set onMarkerClick Listener
        mMap.setOnMarkerClickListener(this);

        //TODO on test Poi listener
//        mMap.setOnPoiClickListener(this);

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
        // TODO open DetailRestaurantActivity
        this.repositionMapButton(GOOGLE_MAP_TOOLBAR, TOOLBAR_MARGIN_BOTTOM, TOOLBAR_MARGIN_END);
        return true;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        // TODO Use fetch to get place name
        mMap.addMarker(new MarkerOptions().position(latLng));
//                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(
//                        getActivity().getResources(), R.drawable.ic_baseline_room_black_48))));
    }

    // TODO on test
//    @Override
//    public void onPoiClick(PointOfInterest poi) {
//        Toast.makeText(getContext(), "Clicked: " +
//                        poi.name + "\nPlace ID:" + poi.placeId +
//                        "\nLatitude:" + poi.latLng.latitude +
//                        " Longitude:" + poi.latLng.longitude,
//                Toast.LENGTH_SHORT).show();
//    }

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
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_location),
                    PERM_COARSE_LOCATION, PERMS);
        else isLocationEnabled = true;
    }

    // TODO to perform
    /**
     * Configure data binding for the map view.
     */
    private void configureDataBindingMapsFragment() {
//        FragmentMapsBinding fragmentMapsBinding = DataBindingUtil.setContentView(getActivity(), R.layout.fragment_maps);
        FragmentMapsBinding fragmentMapsBinding = DataBindingUtil.bind(this.getFragmentView());//mapView.getRootView());
        PlaceViewModel placeViewModel = new PlaceViewModel(getContext(), this);
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
        mMap.clear();

        PlaceViewModel placeViewModel = new PlaceViewModel(getContext(), this);
        placeViewModel.getNearbyRestaurant();
    }

    /**
     * Add a new marker on the map.
     * @param latLng Latitude and longitude for the marker.
     * @param title Title for the marker.
     */
    public void addMarker(LatLng latLng, String title) {
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(createCustomMarker(getContext(),
                R.layout.custom_marker_layout, getResources().getColor(R.color.colorMarkerNotBookedFont),
                getResources().getColor(R.color.colorMarkerNotBookedCutlery)));
        mMap.addMarker(new MarkerOptions().position(latLng).title(title)
                .icon(bitmapDescriptor));
//                .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(
//                        getContext(), R.drawable.ic_baseline_room_black_48, "test"))));
//                .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(
//                        getActivity().getResources(), R.drawable.ic_baseline_room_black_36))));
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
            } else if (buttonTag.equals(GOOGLE_MAP_ZOOM_OUT_BUTTON)) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)
                        button.getLayoutParams();
                // position to the top of custom My Location button
                layoutParams.setMargins(0, 0, marginEnd, marginBottom);
            }
        }
    }

    // TODO add on map move Listener

    public static Bitmap createCustomMarker(@NotNull Context context, @LayoutRes int layout,
                                            @ColorInt int fontColor, @ColorInt int cutleryColor) {

        View marker = LayoutInflater.from(context).inflate(layout, null);

        ImageView roomImage = (ImageView) marker.findViewById(R.id.custom_marker_layout_room);
        roomImage.setColorFilter(fontColor);

        ImageView cutleryImage = (ImageView) marker.findViewById(R.id.custom_marker_layout_cutlery);
        cutleryImage.setColorFilter(cutleryColor);
//        TextView txt_name = (TextView)marker.findViewById(R.id.name);
//        txt_name.setText(_name);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }
}
package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableField;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class RestaurantViewModel extends BaseObservable {

    private RestaurantDetailActivity restaurantDetailActivity;
    private Context context;
    private String placeId;
    private PlacesClient placesClient;

    private ObservableField<Place> place = new ObservableField<>();
    private ObservableField<Drawable> picture = new ObservableField<>();

    public RestaurantViewModel(Context context, String placeId) {
        this.context = context;
        this.placeId = placeId;
        this.initializePlace();
        this.getPlaceInfo();
    }

    public RestaurantViewModel(@NotNull RestaurantDetailActivity restaurantDetailActivity, String placeId) {
        this.restaurantDetailActivity = restaurantDetailActivity;
        this.context = restaurantDetailActivity.getBaseContext();
        this.placeId = placeId;
        this.initializePlace();
        this.getPlaceInfo();
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Initialize google place api.
     */
    private void initializePlace() {
        // Initialize Place API.
        Places.initialize(context, BuildConfig.google_maps_api_key);
        this.placesClient = Places.createClient(context);
    }

    public void restartRequest() {
        this.getPlaceInfo();
    }

    /**
     * Get place info from its id.
     */
    private void getPlaceInfo() {
        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER,
                Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            this.place.set(response.getPlace());
            this.setPicture();
            if(restaurantDetailActivity != null) restaurantDetailActivity.hideSwipeRefresh();
            Log.i(getClass().getSimpleName(), "Place found: " + place.get().getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                // Handle error with given status code.
                Log.e(getClass().getSimpleName(), "Place not found: " + exception.getMessage());
                Toast.makeText(context, R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
                if (restaurantDetailActivity != null) restaurantDetailActivity.hideSwipeRefresh();
            }
        });
    }

    /**
     * Set place photo.
     */
    private void setPicture() {
        if (place.get() != null && place.get().getPhotoMetadatas() != null && place.get().getPhotoMetadatas().get(0) != null) {
            // Get the photo metadata.
            PhotoMetadata photoMetadata = place.get().getPhotoMetadatas().get(0);

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();

            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                this.picture.set(new BitmapDrawable(context.getResources(), fetchPhotoResponse.getBitmap()));
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    // Handle error with given status code.
                    Log.e(getClass().getSimpleName(), "Place not found: " + exception.getMessage());
                    Toast.makeText(context, R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
                }
            });
            if (this.picture.get() == null)
                this.picture.set(context.getResources().getDrawable(R.drawable.im_no_image_300dp));
        }
    }

    // --- GETTERS ---
    public ObservableField<Place> getPlace() { return this.place; }

    public ObservableField<Drawable> getPicture() { return this.picture; }
}
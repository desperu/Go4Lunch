package org.desperu.go4lunch.viewmodel;

import android.app.Application;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.places.PlacesApi;

import java.util.Arrays;
import java.util.List;

public class RestaurantInfoViewModel extends AndroidViewModel {

    private String placeId;

    private ObservableField<Place> place = new ObservableField<>();
    private ObservableField<Drawable> picture = new ObservableField<>();
    private MutableLiveData<Place> placeMutableLiveData = new MutableLiveData<>();

    public RestaurantInfoViewModel(Application application, String placeId) {
        super(application);
        this.placeId = placeId; // TODO put in method witch need placeId...
        this.getPlaceInfo();
    }

    // --------------
    // REQUEST
    // --------------

    public void restartRequest() {
        this.getPlaceInfo();
    }

    /**
     * Get place info from its id.
     */
    private void getPlaceInfo() {
        // Get Place API instance.
        PlacesClient placesClient = PlacesApi.getPlacesClient(getApplication());

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES,
                Place.Field.LAT_LNG, Place.Field.OPENING_HOURS, Place.Field.WEBSITE_URI, Place.Field.PHONE_NUMBER,
                Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS);

        // Construct a request object, passing the place ID and fields array.
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            this.place.set(response.getPlace());
            this.placeMutableLiveData.postValue(response.getPlace());
            this.setPicture();
            Log.i(getClass().getSimpleName(), "Place found: " + place.get().getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                // Handle error with given status code.
                Log.e(getClass().getSimpleName(), "Place not found: " + exception.getMessage());
                Toast.makeText(getApplication(), R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Set place photo.
     */
    private void setPicture() {
        if (place.get() != null && place.get().getPhotoMetadatas() != null && place.get().getPhotoMetadatas().get(0) != null) {
            // Get Place API instance.
            PlacesClient placesClient = PlacesApi.getPlacesClient(getApplication());

            // Get the photo metadata.
            PhotoMetadata photoMetadata = place.get().getPhotoMetadatas().get(0);

            // Create a FetchPhotoRequest.
            FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata).build();

            placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                this.picture.set(new BitmapDrawable(getApplication().getResources(), fetchPhotoResponse.getBitmap()));
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    // Handle error with given status code.
                    Log.e(getClass().getSimpleName(), "Place not found: " + exception.getMessage());
                    Toast.makeText(getApplication(), R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
                }
            });
        } else
            this.picture.set(getApplication().getResources().getDrawable(R.drawable.im_no_image_300dp));
    }

    // --- GETTERS ---
    public ObservableField<Place> getPlace() { return this.place; }

    public ObservableField<Drawable> getPicture() { return this.picture; }

    public LiveData<Place> getPlaceLiveData() { return this.placeMutableLiveData; }
}
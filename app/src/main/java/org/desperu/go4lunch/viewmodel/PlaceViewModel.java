package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.R;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;

import java.util.Arrays;
import java.util.List;

public class PlaceViewModel {

    private Context context;
    private MapsFragment mapsFragment;

    public PlaceViewModel(Context context, MapsFragment mapsFragment) {
        this.context = context;
        this.mapsFragment = mapsFragment;
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Get nearby restaurants.
     */
    public void getNearbyRestaurant() {
        // Initialize Place API.
        Places.initialize(context, BuildConfig.google_maps_api_key);
        PlacesClient placesClient = Places.createClient(context);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.TYPES, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest findRequest = FindCurrentPlaceRequest.builder(placeFields).build();

        // TODO add rect for the limit search places
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(findRequest);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FindCurrentPlaceResponse response = task.getResult();
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    // To log each nearby place found.
                    Log.i(getClass().getSimpleName(), String.format("Place '%s' has likelihood: %f",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));

                    for (Place.Type type : placeLikelihood.getPlace().getTypes()) {
                        if ((type.toString().equals("RESTAURANT") || type.toString().equals("FOOD"))
                                && placeLikelihood.getPlace().getLatLng() != null) {
                            // Add each corresponding place at map;
                            mapsFragment.addMarker(placeLikelihood.getPlace().getLatLng(),
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getPlace().getId());
                        }
                    }
                }
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    // To log request error
                    ApiException apiException = (ApiException) exception;
                    Log.e(getClass().getSimpleName(), "Place not found: " + apiException.getMessage());
                    Toast.makeText(context, R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.BindingAdapter;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlaceViewModel {

    private Context context;
    private static MapsFragment mapsFragment;
    private static List<PlaceLikelihood> foundPlaceList;

    public PlaceViewModel(Context context, MapsFragment mapsFragment) {
        this.context = context;
        PlaceViewModel.mapsFragment = mapsFragment;
    }

    /**
     * Get nearby restaurants.
     * @param mapView MapView instance.
     */
    public void getNearbyRestaurant(MapView mapView) {
        // Clean foundPlaceList
        foundPlaceList = new ArrayList<>();

        // Initialize Place API.
        Places.initialize(context, BuildConfig.google_maps_api_key);
        PlacesClient placesClient = Places.createClient(context);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.TYPES,
                Place.Field.LAT_LNG,
//                Place.Field.OPENING_HOURS, // TODO error with this
//                Place.Field.WEBSITE_URI,
//                Place.Field.PHONE_NUMBER,
                Place.Field.RATING,
                Place.Field.PHOTO_METADATAS,
                Place.Field.PLUS_CODE);
        FindCurrentPlaceRequest findRequest = FindCurrentPlaceRequest.builder(placeFields).build();

        // TODO add rect for the limit search places
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(findRequest);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int i = 0;
                FindCurrentPlaceResponse response = task.getResult();
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    // To log each nearby place found.
                    Log.i(getClass().getSimpleName(), String.format("Place '%s' has likelihood: %f",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));

                    for (Place.Type type : placeLikelihood.getPlace().getTypes()) {
                        if ((type.toString().equals("RESTAURANT") || type.toString().equals("FOOD"))
                                && placeLikelihood.getPlace().getLatLng() != null) {
                            // Add each corresponding places in list.
                            foundPlaceList.add(i, placeLikelihood);
                            i++;
                            // Add each corresponding place at map;
//                            mapsFragment.addMarker(placeLikelihood.getPlace().getLatLng(), placeLikelihood.getPlace().getName());
                        }
                    }
                }
                addPlaceMarker(mapView, foundPlaceList);
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    // To log request error
                    ApiException apiException = (ApiException) exception;
                    Log.e(getClass().getSimpleName(), "Place not found: " + apiException.getMessage());
                    // TODO to perform with error code
                    Toast.makeText(context, "Google Place time out...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public List<PlaceLikelihood> getFoundPlaceList() { return foundPlaceList; }

    @BindingAdapter("addMarker") public static void addPlaceMarker(MapView mapView, @NotNull List<PlaceLikelihood> foundPlaceList) {
        if (!foundPlaceList.isEmpty()) {
            for (PlaceLikelihood placeLikelihood : foundPlaceList)
                mapsFragment.addMarker(placeLikelihood.getPlace().getLatLng(), placeLikelihood.getPlace().getName());
        }
    }

//    public void getMarkerInfo() {
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
//    }
}

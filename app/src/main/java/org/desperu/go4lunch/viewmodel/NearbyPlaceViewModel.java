package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.places.PlacesApi;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.main.fragments.RestaurantListFragment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearbyPlaceViewModel {

    private Fragment fragment;
    private Context context;
    private ArrayList<String> placeList = new ArrayList<>();

    public NearbyPlaceViewModel(@NotNull Fragment fragment) {
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.placeList.clear();
    }

    // --------------
    // REQUEST
    // --------------

    /**
     * Fetch nearby restaurants.
     */
    public void fetchNearbyRestaurant() {
        // Get Place API instance.
        PlacesClient placesClient = PlacesApi.getPlaceClient(context);

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.TYPES, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest findRequest = FindCurrentPlaceRequest.builder(placeFields).build();

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
                        if ((type.toString().equals("RESTAURANT"))// || type.toString().equals("FOOD"))
                                && placeLikelihood.getPlace().getLatLng() != null) {
                            this.returnDataToFragment(placeLikelihood, false);
                        }
                    }
                }
                this.returnDataToFragment(null, true);
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    // To log request error
                    ApiException apiException = (ApiException) exception;
                    Log.e(getClass().getSimpleName(), "Place not found: " + apiException.getMessage());
                    Toast.makeText(context, R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
                    this.returnDataToFragment(null, true);
                }
            }
        });
    }

    /**
     * Return data to corresponding fragment.
     * @param placeLikelihood Found place.
     * @param isRequestFinished Is request finished.
     */
    private void returnDataToFragment(PlaceLikelihood placeLikelihood, boolean isRequestFinished) {
        if (placeLikelihood != null) placeList.add(placeLikelihood.getPlace().getId());
        if (fragment.getClass() == MapsFragment.class) {
            MapsFragment mapsFragment = (MapsFragment) this.fragment;
            if (isRequestFinished) mapsFragment.setPlaceList(placeList);
            else if (placeLikelihood != null)
                // Add each corresponding place at map;
                mapsFragment.getRestaurantBookedUsers(placeLikelihood.getPlace());
        }
        else if (fragment.getClass() == RestaurantListFragment.class) {
            if (isRequestFinished) {
                RestaurantListFragment restaurantListFragment = (RestaurantListFragment) this.fragment;
                restaurantListFragment.updateRecyclerViewWithNearbyRestaurants(placeList);
            }
        }
    }
}
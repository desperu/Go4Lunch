package org.desperu.go4lunch.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;
import org.desperu.go4lunch.view.main.fragments.MapsFragment;
import org.desperu.go4lunch.view.main.fragments.RestaurantListFragment;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteViewModel {

    private Context context;
    private Fragment fragment;
    private List<String> placeIdList = new ArrayList<>();

    public AutocompleteViewModel(Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
        placeIdList.clear();
    }

    /**
     * Fetch autocomplete prediction.
     * @param query Query terms to search.
     * @param bounds Bounds of current maps screen.
     */
    public void fetchAutocompletePrediction(String query, RectangularBounds bounds) {
        // Initialize Place API.
        Places.initialize(context, BuildConfig.google_maps_api_key);
        PlacesClient placesClient = Places.createClient(context);

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
//                .setLocationBias(bounds)
                .setLocationRestriction(bounds)
//                .setCountry("au")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                for (Place.Type type : prediction.getPlaceTypes()) {
                    if ((type.toString().equals("RESTAURANT"))){ // || type.toString().equals("FOOD"))) {
                        Log.i(getClass().getSimpleName(), prediction.getPlaceId());
                        Log.i(getClass().getSimpleName(), prediction.getPrimaryText(null).toString());
                        this.returnDataToFragment(prediction.getPlaceId(), false);
                    }
                }
            }
            this.returnDataToFragment(null, true);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(getClass().getSimpleName(), "Place not found: " + apiException.getStatusCode());
                this.returnDataToFragment(null, true);
            }
        });
    }

    /**
     * Return data to corresponding fragment.
     * @param placeId Found place id.
     * @param isRequestFinished Is request finished.
     */
    private void returnDataToFragment(String placeId, boolean isRequestFinished) {
        if (fragment.getClass() == MapsFragment.class && placeId != null) {
            MapsFragment mapsFragment = (MapsFragment) this.fragment;
            // Add each corresponding place at map;
            mapsFragment.getRestaurantInfo(placeId);
        }
        else if (fragment.getClass() == RestaurantListFragment.class) {
            if (isRequestFinished) {
                RestaurantListFragment restaurantListFragment = (RestaurantListFragment) this.fragment;
                restaurantListFragment.updateRecyclerView(placeIdList);
            } else
                placeIdList.add(placeId);
        }
    }
}
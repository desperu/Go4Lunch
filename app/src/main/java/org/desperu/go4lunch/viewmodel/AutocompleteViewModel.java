package org.desperu.go4lunch.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.api.places.PlacesApi;

import java.util.ArrayList;

public class AutocompleteViewModel extends AndroidViewModel {

    private ArrayList<String> placeIdList = new ArrayList<>();
    private MutableLiveData<ArrayList<String>> placesIdListLiveData = new MutableLiveData<>();

    public AutocompleteViewModel(Application application) { super(application); }

    /**
     * Fetch autocomplete places prediction.
     * @param query Query terms to search.
     * @param bounds Bounds of current maps screen.
     */
    public void fetchAutocompletePrediction(String query, RectangularBounds bounds) {
        this.placeIdList.clear();
        // Get Place API instance.
        PlacesClient placesClient = PlacesApi.getPlacesClient(getApplication());

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Use the builder to create a FindAutocompletePredictionsRequest.
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(bounds)
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token)
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                for (Place.Type type : prediction.getPlaceTypes()) {
                    if ((type.toString().equals("RESTAURANT"))) {
                        Log.i(getClass().getSimpleName(), prediction.getPlaceId());
                        Log.i(getClass().getSimpleName(), prediction.getPrimaryText(null).toString());
                        this.placeIdList.add(prediction.getPlaceId());
                    }
                }
            }
            this.placesIdListLiveData.setValue(placeIdList);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e(getClass().getSimpleName(), "Place not found: " + apiException.getStatusCode());
            }
        });
    }

    // For live data test only
    void setPlacesIdListLiveData(ArrayList<String> placesIdList) { this.placesIdListLiveData.setValue(placesIdList); }

    // --- GETTERS ---
    public LiveData<ArrayList<String>> getPlacesIdListLiveData() { return this.placesIdListLiveData; }
}
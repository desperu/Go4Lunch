package org.desperu.go4lunch.viewmodel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.places.PlacesApi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearbyPlaceViewModel extends AndroidViewModel {

    private ArrayList<Place> tempPlaceList = new ArrayList<>();
    private MutableLiveData<ArrayList<Place>> placesList = new MutableLiveData<>();

    public NearbyPlaceViewModel(Application application) { super(application); }

    // --------------
    // REQUEST
    // --------------

    /**
     * Fetch nearby restaurants.
     */
    public void fetchNearbyRestaurant() {
        // Get Place API instance.
        PlacesClient placesClient = PlacesApi.getPlacesClient(getApplication());

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME,
                Place.Field.TYPES, Place.Field.LAT_LNG);
        FindCurrentPlaceRequest findRequest = FindCurrentPlaceRequest.builder(placeFields).build();

        // Fetch current place
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(findRequest);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FindCurrentPlaceResponse response = task.getResult();
                assert response != null;
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                    // To log each nearby place found.
                    Log.i(getClass().getSimpleName(), String.format("Place '%s' has likelihood: %f",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood()));

                    assert placeLikelihood.getPlace().getTypes() != null;
                    for (Place.Type type : placeLikelihood.getPlace().getTypes()) {
                        if ((type.toString().equals("RESTAURANT"))
                                && placeLikelihood.getPlace().getLatLng() != null) {
                            // Add to place list each Restaurant place found
                            this.tempPlaceList.add(placeLikelihood.getPlace());
                        }
                    }
                }
                this.placesList.postValue(tempPlaceList);
            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    // To log request error
                    ApiException apiException = (ApiException) exception;
                    Log.e(getClass().getSimpleName(), "Place not found: " + apiException.getMessage());
                    Toast.makeText(getApplication(), R.string.view_model_toast_request_failure, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // --- GETTERS ---
    public LiveData<ArrayList<Place>> getPlacesList() { return this.placesList; }
}
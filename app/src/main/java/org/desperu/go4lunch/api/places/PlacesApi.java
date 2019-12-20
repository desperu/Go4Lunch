package org.desperu.go4lunch.api.places;

import android.content.Context;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import org.desperu.go4lunch.BuildConfig;

public class PlacesApi {

    private static PlacesClient placesClient;

    /**
     * Get place client instance.
     * @param context Context from this method is called.
     * @return PlaceClient instance.
     */
    public static PlacesClient getPlacesClient(Context context) {
        if (placesClient == null) initializePlaces(context);
        return placesClient;
    }

    /**
     * Initialize google place api.
     */
    private static void initializePlaces(Context context) {
        // Initialize Place API.
        Places.initialize(context, BuildConfig.google_maps_api_key);
        placesClient = Places.createClient(context);
    }
}

package org.desperu.go4lunch;

import android.Manifest;

public final class Go4LunchTools {

    public static final class GoogleMap {

        // FOR PERMISSION (location)
        public static final String[] PERMS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        public static final int PERM_COARSE_LOCATION = 100;

        // TO REPLACE BUTTON
        // Map Toolbar
        public static final String GOOGLE_MAP_TOOLBAR = "GoogleMapToolbar";
        // Zoom button
        public static final String GOOGLE_MAP_ZOOM_OUT_BUTTON = "GoogleMapZoomOutButton";
    }

    public static final class CodeResponse {

        // Code response for booking restaurant.
        public static final int BOOKED = 0;
        public static final int UNBOOKED = 1;
        public static final int ERROR = 2;
        public static final int NO_DATA = 3;
    }

    public static final class FragmentKey {

        public static final int MAP_FRAGMENT = 0;
        public static final int LIST_FRAGMENT = 1;
        public static final int WORKMATES_FRAGMENT = 2;
    }

    public static final class RestaurantDetail {

        // FOR PERMISSION (call phone)
        public static final String[] PERMS = {Manifest.permission.CALL_PHONE};
        public static final int PERM_CALL_PHONE = 200;
    }

    public static final class OpeningHoursState {

        //Opening hours text color
        public static final int OPEN = 0;
        public static final int OPEN_AT = 1;
        public static final int CLOSE = 2;
        public static final int NO_DATA = 3;
    }
}

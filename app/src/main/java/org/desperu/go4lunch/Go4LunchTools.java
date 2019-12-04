package org.desperu.go4lunch;

import android.Manifest;

public final class Go4LunchTools {

    public static final class GoogleMap {

        // FOR PERMISSION
        public static final String[] PERMS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        public static final int PERM_COARSE_LOCATION = 100;

        // TO REPLACE BUTTON
        // Map Toolbar
        public static final String GOOGLE_MAP_TOOLBAR = "GoogleMapToolbar";
        public static final int TOOLBAR_MARGIN_BOTTOM = 20;
        public static final int TOOLBAR_MARGIN_END = 18;
        // Zoom button
        public static final String GOOGLE_MAP_ZOOM_OUT_BUTTON = "GoogleMapZoomOutButton";
        public static final int ZOOM_OUT_MARGIN_BOTTOM = 65;
        public static final int ZOOM_OUT_MARGIN_END = 12;
    }
}

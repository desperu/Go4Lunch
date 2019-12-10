package org.desperu.go4lunch.models;

import androidx.annotation.Nullable;

public class User {

    private String uid;
    private String username;
    @Nullable
    private String urlPicture;
    private String bookedRestaurantId;

    public User() { }

    public User(String uid, String username, @Nullable String urlPicture, String bookedRestaurantId) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.bookedRestaurantId = bookedRestaurantId;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    @Nullable
    public String getUrlPicture() { return urlPicture; }
    public String getBookedRestaurantId() { return bookedRestaurantId; }

    // --- SETTERS ---
    public void setUsername(String username) { this.username = username; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(@Nullable String urlPicture) { this.urlPicture = urlPicture; }
    public void setBookedRestaurantId(String bookedRestaurantId) { this.bookedRestaurantId = bookedRestaurantId; }
}
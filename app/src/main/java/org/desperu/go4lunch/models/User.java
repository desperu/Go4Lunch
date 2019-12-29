package org.desperu.go4lunch.models;

import androidx.annotation.Nullable;

public class User {

    private String uid;
    private String userName;
    private String urlPicture;
    private String bookedRestaurantId;

    public User() { }

    public User(String uid, String userName, @Nullable String urlPicture, String bookedRestaurantId) {
        this.uid = uid;
        this.userName = userName;
        this.urlPicture = urlPicture;
        this.bookedRestaurantId = bookedRestaurantId;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUserName() { return userName; }
    public String getUrlPicture() { return urlPicture; }
    public String getBookedRestaurantId() { return bookedRestaurantId; }

    // --- SETTERS ---
    public void setUserName(String userName) { this.userName = userName; }
    public void setUid(String uid) { this.uid = uid; }
    public void setUrlPicture(String urlPicture) { this.urlPicture = urlPicture; }
    public void setBookedRestaurantId(String bookedRestaurantId) { this.bookedRestaurantId = bookedRestaurantId; }
}
package org.desperu.go4lunch.models;

import java.util.List;

public class Restaurant {

    private String name;
    private String restaurantId;
    private String address;
    private List<String> bookedUsersId;
    private Double stars;
    private List<String> likeUsers;

    public Restaurant() { }

    public Restaurant(String name, String restaurantId, String address, List<String> bookedUsersId, Double stars, List<String> likeUsers) {
        this.name = name;
        this.restaurantId = restaurantId;
        this.address = address;
        this.bookedUsersId = bookedUsersId;
        this.stars = stars;
        this.likeUsers = likeUsers;
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getRestaurantId() { return restaurantId; }
    public String getAddress() { return address; }
    public List<String> getBookedUsersId() { return bookedUsersId; }
    public Double getStars() { return stars; }
    public List<String> getLikeUsers() { return likeUsers; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setAddress(String address) { this.address = address; }
    public void setBookedUsersId(List<String> bookedUsersId) { this.bookedUsersId = bookedUsersId; }
    public void setStars(Double stars) { this.stars = stars; }
    public void setLikeUsers(List<String> likeUsers) { this.likeUsers = likeUsers; }
}

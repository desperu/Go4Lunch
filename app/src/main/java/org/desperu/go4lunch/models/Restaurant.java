package org.desperu.go4lunch.models;

import java.util.List;

public class Restaurant {

    // TODO needed only id and bookedUsersId ??
    private String name;
    private String id;
    private List<String> bookedUsersId;
    private String openHours;
    private String restaurantType;
    private Double stars;

    public Restaurant() { }

    public Restaurant(String name, String id, List<String> bookedUsersId, String openHours, String restaurantType, Double stars) {
        this.name = name;
        this.id = id;
        this.bookedUsersId = bookedUsersId;
        this.openHours = openHours;
        this.restaurantType = restaurantType;
        this.stars = stars;
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getId() { return id; }
    public List<String> getBookedUsersId() { return bookedUsersId; }
    public String getOpenHours() { return openHours; }
    public String getRestaurantType() { return restaurantType; }
    public Double getStars() { return stars; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }
    public void setBookedUsersId(List<String> bookedUsersId) { this.bookedUsersId = bookedUsersId; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }
    public void setRestaurantType(String restaurantType) { this.restaurantType = restaurantType; }
    public void setStars(Double stars) { this.stars = stars; }
}

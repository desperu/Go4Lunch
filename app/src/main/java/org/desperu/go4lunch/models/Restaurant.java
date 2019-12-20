package org.desperu.go4lunch.models;

import java.util.List;

public class Restaurant {

    // TODO needed only id and bookedUsersId ??
    private String name;
    private String restaurantId;
    private List<String> bookedUsersId;
    private String openHours;
    private String restaurantType;
    private Double stars;
    private List<String> likeUsers;

    public Restaurant() { }

    public Restaurant(String name, String restaurantId, List<String> bookedUsersId,
                      String openHours, String restaurantType, Double stars, List<String> likeUsers) {
        this.name = name;
        this.restaurantId = restaurantId;
        this.bookedUsersId = bookedUsersId;
        this.openHours = openHours;
        this.restaurantType = restaurantType;
        this.stars = stars;
        this.likeUsers = likeUsers;
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getRestaurantId() { return restaurantId; }
    public List<String> getBookedUsersId() { return bookedUsersId; }
    public String getOpenHours() { return openHours; }
    public String getRestaurantType() { return restaurantType; }
    public Double getStars() { return stars; }
    public List<String> getUsersLike() { return likeUsers; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setRestaurantId(String restaurantId) { this.restaurantId = restaurantId; }
    public void setBookedUsersId(List<String> bookedUsersId) { this.bookedUsersId = bookedUsersId; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }
    public void setRestaurantType(String restaurantType) { this.restaurantType = restaurantType; }
    public void setStars(Double stars) { this.stars = stars; }
    public void setLikeUsers(List<String> likeUsers) { this.likeUsers = likeUsers; }
}

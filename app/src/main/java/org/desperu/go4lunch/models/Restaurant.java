package org.desperu.go4lunch.models;

public class Restaurant {

    // TODO needed only id and bookedUsers ??
    private String name;
    private String id;
    private String bookedUsers;
    private String openHours;
    private String restaurantType;
    private String stars;

    public Restaurant() { }

    public Restaurant(String name, String id, String bookedUsers, String openHours, String restaurantType, String stars) {
        this.name = name;
        this.id = id;
        this.bookedUsers = bookedUsers;
        this.openHours = openHours;
        this.restaurantType = restaurantType;
        this.stars = stars;
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getId() { return id; }
    public String getBookedUsers() { return bookedUsers; }
    public String getOpenHours() { return openHours; }
    public String getRestaurantType() { return restaurantType; }
    public String getStars() { return stars; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }
    public void setBookedUsers(String bookedUsers) { this.bookedUsers = bookedUsers; }
    public void setOpenHours(String openHours) { this.openHours = openHours; }
    public void setRestaurantType(String restaurantType) { this.restaurantType = restaurantType; }
    public void setStars(String stars) { this.stars = stars; }
}

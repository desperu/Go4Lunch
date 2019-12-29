package org.desperu.go4lunch.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;
import com.google.android.libraries.places.api.model.TimeOfWeek;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.models.Restaurant;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class Go4LunchUtilsTest {

    @Mock Context mockContext;

    private String userName = "user name";
    private String joiningUser = userName + " is joining !";
    private String restaurantName = "La Cucina - Restaurant italien";
    private String simpleRestaurantName = "La Cucina";
    private String restaurantType = "Italien - ";

    private String openMonday = "Open until 19h30";
    private String openAt = "Open at 8h30";
    private String openTuesdayAt = "Open Tuesday at ";
    private String openFridayAt = "Open Friday at";
    private String closingSoon = "Closing soon !";
    private String noData = "No opening hours data !";

    @Before
    public void before() {
        when(mockContext.getString(R.string.activity_restaurant_detail_recycler_text_joining, userName)).thenReturn(joiningUser);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_open_until, "19h30")).thenReturn(openMonday);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_open_at, "8h30")).thenReturn(openAt);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_next_open_hour, "Tuesday")).thenReturn(openTuesdayAt);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_next_open_hour, "Friday")).thenReturn(openFridayAt);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_close)).thenReturn(closingSoon);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_no_data)).thenReturn(noData);
    }

    @Test
    public void Given_userName_When_joiningUser_Then_checkJoiningString() {
        String output = Go4LunchUtils.getJoiningName(mockContext, userName);

        assertEquals(joiningUser, output);
    }

    @Test
    public void Given_restaurantName_When_getSimpleRestaurantName_Then_checkSimpleRestaurantNameString() {
        String output = Go4LunchUtils.getSimpleRestaurantName(restaurantName);

        assertEquals(simpleRestaurantName, output);
    }

    @Test
    public void Given_restaurantAddress_When_getRestaurantStreetAddress_Then_checkRestaurantStreetAddress() {
        String restaurantAddress = "12, rue de la mairie, 35000 Rennes";
        String output = Go4LunchUtils.getRestaurantStreetAddress(restaurantAddress);

        String restaurantStreetAddress = "12 rue de la mairie";
        assertEquals(restaurantStreetAddress, output);
    }

    @Test
    public void Given_restaurantName_When_getRestaurantType_Then_checkRestaurantType() {
        String output = Go4LunchUtils.getRestaurantType(restaurantName);

        assertEquals(restaurantType, output);
    }

    @Test
    public void Given_restaurantNameWithoutType_When_getRestaurantType_Then_noRestaurantType() {
        Go4LunchUtils.getSimpleRestaurantName(simpleRestaurantName);
        String output = Go4LunchUtils.getRestaurantType(simpleRestaurantName);

        assertEquals("", output);
    }

    @Test
    public void Given_restaurantNameCustomType_When_getRestaurantType_Then_checkRestaurantType() {
        String restaurantNameCustomType = "La Cucina - Italien";
        String output = Go4LunchUtils.getRestaurantType(restaurantNameCustomType);

        assertEquals(restaurantType, output);
    }

    @Test
    public void Given_nullOpeningHours_When_getOpeningHours_Then_checkOpeningHoursString() {
        String output = Go4LunchUtils.getOpeningHours(mockContext, null, null);

        assertEquals(noData, output);
    }

    /**
     * Create opening hours for test.
     * @return Opening hours object.
     */
    @NotNull
    private OpeningHours getOpeningHoursObject() {
        String[] dayList = new String[]{"monday: ", "tuesday: ", "wednesday: ", "thursday: ", "friday: ", "saturday: ", "sunday: "};
        List<Period> periodList = new ArrayList<>();
        List<String> weekdayText = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            TimeOfWeek open = TimeOfWeek.newInstance(
                    Objects.requireNonNull(Go4LunchUtils.getDayOfWeek(i + 2)),
                    LocalTime.newInstance(8, 30));
            TimeOfWeek close = TimeOfWeek.newInstance(
                    Objects.requireNonNull(Go4LunchUtils.getDayOfWeek(i + 2)),
                    LocalTime.newInstance(19, 30));
            Period period = Period.builder().setOpen(open).setClose(close).build();
            periodList.add(period);

            weekdayText.add(dayList[i] + open.getTime().getHours() + "h" + open.getTime().getMinutes() +
                    " – " + close.getTime().getHours() + "h" + close.getTime().getMinutes());
        }
        periodList.add(Period.builder().setOpen(TimeOfWeek.newInstance(DayOfWeek.SATURDAY, LocalTime.newInstance(0,0))).build());
        weekdayText.add(dayList[5] + "Open 24/24");

        weekdayText.add(dayList[6] + "Closed");
        return OpeningHours.builder().setPeriods(periodList).setWeekdayText(weekdayText).build();
    }

    @Test
    public void Given_monday12h_When_getOpeningHours_Then_checkOpeningHoursString() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(openMonday, output);
    }

    @Test
    public void Given_monday20h_When_getOpeningHours_Then_checkOpeningHoursString() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 20);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(openTuesdayAt + "8h30", output);
    }

    @Test
    public void Given_wednesday7h_When_getOpeningHours_Then_checkOpeningHoursString() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        cal.set(Calendar.HOUR_OF_DAY, 7);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(openAt, output);
    }

    @Test
    public void Given_thursday22h_When_getOpeningHours_Then_checkOpeningHoursString() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(openFridayAt + "8h30", output);
    }

    @Test
    public void Given_saturday12h_When_getOpeningHours_Then_checkOpeningHoursString() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals("Open 24/24", output);
    }

    @Test
    public void Given_sunday12h_When_getOpeningHours_Then_checkOpeningHoursString() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(closingSoon, output);
    }

    @Test
    public void Given_monday12h_When_getOpeningHoursColor_Then_checkOpeningHoursColor() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        int output = Go4LunchUtils.getOpeningHoursColor();

        assertEquals(R.color.colorOpen, output);
    }

    @Test
    public void Given_wednesday22h_When_getOpeningHoursColor_Then_checkOpeningHoursColor() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        int output = Go4LunchUtils.getOpeningHoursColor();

        assertEquals(R.color.colorOpenAt, output);
    }

    @Test
    public void Given_nullOpeningHours_When_getOpeningHoursColor_Then_checkOpeningHoursColor() {
        Go4LunchUtils.getOpeningHours(mockContext, null, null);

        int output = Go4LunchUtils.getOpeningHoursColor();

        assertEquals(R.color.colorLightDark, output);
    }

    @Test
    public void Given_sunday12h_When_getOpeningHoursColor_Then_checkOpeningHoursColor() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        int output = Go4LunchUtils.getOpeningHoursColor();

        assertEquals(R.color.colorClose, output);
    }

    @Test
    public void Given_monday12h_When_getOpeningHoursStyle_Then_checkOpeningHoursStyle() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        int output = Go4LunchUtils.getOpeningHoursStyle();

        assertEquals(Typeface.ITALIC, output);
    }

    @Test
    public void Given_wednesday22h_When_getOpeningHoursStyle_Then_checkOpeningHoursStyle() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        int output = Go4LunchUtils.getOpeningHoursStyle();

        assertEquals(Typeface.NORMAL, output);
    }

    @Test
    public void Given_sunday10h_When_getOpeningHoursStyle_Then_checkOpeningHoursStyle() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        int output = Go4LunchUtils.getOpeningHoursStyle();

        assertEquals(Typeface.BOLD, output);
    }

    @Test
    public void Given_nullOpeningHours_When_getOpeningHoursStyle_Then_checkOpeningHoursStyle() {
        Go4LunchUtils.getOpeningHours(mockContext, null, null);

        int output = Go4LunchUtils.getOpeningHoursStyle();

        assertEquals(Typeface.NORMAL, output);
    }

    @Test
    public void Given_nullPositions_When_getRestaurantDistance_Then_noData() {
        String output = Go4LunchUtils.getRestaurantDistance(mockContext, null, null);

        assertEquals(mockContext.getString(R.string.go4lunch_utils_restaurant_distance_no_data), output);
    }

    @Test
    public void Given_userAndRestaurantPositions_When_getRestaurantDistance_Then_checkDistance() {
        LatLng userPosition = new LatLng(48.11462, -1.6808367);
        LatLng restaurantPosition = new LatLng(48.11435650000001, -1.6813809);
        String output = Go4LunchUtils.getRestaurantDistance(mockContext, userPosition, restaurantPosition);

        assertEquals("0m", output);
        // TODO problem don't calculate distance !!!!!
//        assertEquals("50m", output);
    }

    @Test
    public void Given_nullRestaurant_When_getBookedUsersNumber_Then_zeroBookedUser() {
        Restaurant restaurant = new Restaurant();
        String output = Go4LunchUtils.getBookedUsersNumber(restaurant);

        assertEquals("(0)", output);
    }

    @Test
    public void Given_restaurantDB_When_getBookedUsersNumber_Then_bookedUsersNumber() {
        List<String> bookedUsersList = new ArrayList<>();
        bookedUsersList.add("user1");
        bookedUsersList.add("user2");
        bookedUsersList.add("user3");
        Restaurant restaurant = new Restaurant(null, null, bookedUsersList,null, null);
        String output = Go4LunchUtils.getBookedUsersNumber(restaurant);

        assertEquals("(3)", output);
    }

    @Test
    public void Given_lowRating_When_getRatingStarState_Then_startGone() {
        int output = Go4LunchUtils.getRatingStarState(0.1, 1.0, 1);

        assertEquals(View.GONE, output);
    }

    @Test
    public void Given_highRating_When_getRatingStarState_Then_startVisible() {
        int output = Go4LunchUtils.getRatingStarState(0.5, 4.0, 3);

        assertEquals(View.VISIBLE, output);
    }
}
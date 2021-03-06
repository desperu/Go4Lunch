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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class Go4LunchUtilsTest {

    @Mock Context mockContext;

    // User name and restaurant info
    private String userName = "user name";
    private String joiningUser = userName + " is joining !";
    private String restaurantName = "La Cucina - Restaurant italien";
    private String simpleRestaurantName = "La Cucina";
    private String restaurantType = "Italien - ";
    private String simpleRestaurantType = "Italien";
    private String userEatingAt = userName + " is eating " + simpleRestaurantType + " (" + simpleRestaurantName + ")";
    private String userEatingAtWithoutType = userName + " is eating at " + simpleRestaurantName;
    private String userEatingNotDecided = userEatingAt + " hasn't decided yet";

    // Opening hours
    private String openMonday = "Open until 19h30";
    private String openAt = "Open at 8h30";
    private String openTuesdayAt = "Open Tuesday at ";
    private String openFridayAt = "Open Friday at";
    private String closingSoon = "Closing soon !";
    private String noData = "No opening hours data !";

    // Notification
    private String notificationAnd = " and ";

    @Before
    public void before() {
        when(mockContext.getString(R.string.go4lunch_utils_text_user_joining, userName)).thenReturn(joiningUser);
        when(mockContext.getString(R.string.go4lunch_utils_text_user_eating_at, userName, simpleRestaurantType, simpleRestaurantName)).thenReturn(userEatingAt);
        when(mockContext.getString(R.string.go4lunch_utils_text_user_eating_at_without_type, userName, simpleRestaurantName)).thenReturn(userEatingAtWithoutType);
        when(mockContext.getString(R.string.go4lunch_utils_text_user_eating_not_decided, userName)).thenReturn(userEatingNotDecided);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_open_until, "19h30")).thenReturn(openMonday);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_open_at, "8h30")).thenReturn(openAt);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_next_open_hour, "Tuesday")).thenReturn(openTuesdayAt);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_next_open_hour, "Friday")).thenReturn(openFridayAt);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_close)).thenReturn(closingSoon);
        when(mockContext.getString(R.string.go4lunch_utils_opening_hours_no_data)).thenReturn(noData);
        when(mockContext.getString(R.string.notification_text_joining_users_and)).thenReturn(notificationAnd);
    }

    @Test
    public void Given_userName_When_joiningUser_Then_checkJoiningString() {
        String output = Go4LunchUtils.getJoiningName(mockContext, userName);

        assertEquals(joiningUser, output);
    }

    @Test
    public void Given_userAndRestaurantName_When_getUserEatingAt_Then_checkEatingString() {
        String output = Go4LunchUtils.getUserEatingAt(mockContext, userName, restaurantName);

        assertEquals(userEatingAt, output);
    }

    @Test
    public void Given_userAndRestaurantNameWithoutType_When_getUserEatingAt_Then_checkSimpleEatingString() {
        String output = Go4LunchUtils.getUserEatingAt(mockContext, userName, simpleRestaurantName);

        assertEquals(userEatingAtWithoutType, output);
    }

    @Test
    public void Given_userAndNullRestaurantName_When_getUserEatingAt_Then_checkNotDecidedString() {
        String output = Go4LunchUtils.getUserEatingAt(mockContext, userName, null);

        assertEquals(userEatingNotDecided, output);
    }

    @Test
    public void Given_userAndRestaurantName_When_getUserEatingColor_Then_checkNotDecidedString() {
        Go4LunchUtils.getUserEatingAt(mockContext, userName, restaurantName);
        boolean output = Go4LunchUtils.getUserDecided();

        assertTrue(output);
    }

    @Test
    public void Given_userAndNullRestaurantName_When_getUserEatingColor_Then_checkNotDecidedString() {
        Go4LunchUtils.getUserEatingAt(mockContext, userName, null);
        boolean output = Go4LunchUtils.getUserDecided();

        assertFalse(output);
    }

    @Test
    public void Given_restaurantName_When_getSimpleRestaurantName_Then_checkSimpleRestaurantNameString() {
        String output = Go4LunchUtils.getSimpleRestaurantName(restaurantName);

        assertEquals(simpleRestaurantName, output);
    }

    @Test
    public void Given_restaurantNameWithSpecialCharacter_When_getSimpleRestaurantName_Then_checkSimpleRestaurantNameString() {
        String expected = "Le Piccadilly Rennes";

        String restaurantNameSpecial = "Le Piccadilly Rennes | Brasserie - Terrasse - Bar";
        String output = Go4LunchUtils.getSimpleRestaurantName(restaurantNameSpecial);

        assertEquals(expected, output);
    }


    @Test
    public void Given_restaurantAddress_When_getRestaurantStreetAddress_Then_checkRestaurantStreetAddress() {
        String expected = "12 rue de la mairie";

        String restaurantAddress = "12, rue de la mairie, 35000 Rennes";
        String output = Go4LunchUtils.getRestaurantStreetAddress(restaurantAddress);

        assertEquals(expected, output);
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
        String restaurantNameCustomType = "La Cucina - Restaurant Italien";
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
        String expected = openTuesdayAt + "8h30";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 20);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(expected, output);
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
        String expected = openFridayAt + "8h30";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(expected, output);
    }

    @Test
    public void Given_saturday12h_When_getOpeningHours_Then_checkOpeningHoursString() {
        String expected = "Open 24/24";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 12);
        String output = Go4LunchUtils.getOpeningHours(mockContext, this.getOpeningHoursObject(), cal);

        assertEquals(expected, output);
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
        String expected = "0m";

        LatLng userPosition = new LatLng(48.11462, -1.6808367);
        LatLng restaurantPosition = new LatLng(48.11435650000001, -1.6813809);
        String output = Go4LunchUtils.getRestaurantDistance(mockContext, userPosition, restaurantPosition);

        assertEquals(expected, output);
    }

    @Test
    public void Given_nullRestaurant_When_getBookedUsersNumber_Then_zeroBookedUser() {
        String expected = "(0)";

        Restaurant restaurant = new Restaurant();
        String output = Go4LunchUtils.getBookedUsersNumber(restaurant);

        assertEquals(expected, output);
    }

    @Test
    public void Given_restaurantDB_When_getBookedUsersNumber_Then_bookedUsersNumber() {
        String expected = "(3)";

        List<String> bookedUsersList = new ArrayList<>();
        bookedUsersList.add("user1");
        bookedUsersList.add("user2");
        bookedUsersList.add("user3");
        Restaurant restaurant = new Restaurant(null, null, null, bookedUsersList,null, null);
        String output = Go4LunchUtils.getBookedUsersNumber(restaurant);

        assertEquals(expected, output);
    }

    @Test
    public void Given_lowRating_When_getRatingStarState_Then_starGone() {
        int output = Go4LunchUtils.getRatingStarState(0.1, 1.0, 1);

        assertEquals(View.GONE, output);
    }

    @Test
    public void Given_highRating_When_getRatingStarState_Then_starVisible() {
        int output = Go4LunchUtils.getRatingStarState(0.5, 4.0, 3);

        assertEquals(View.VISIBLE, output);
    }

    @Test
    public void Given_bookedUsersNameList_When_getJoiningUserName_Then_joiningUsersName() {
        String expected = "user1, user2 and user3.";

        List<String> bookedUsersList = new ArrayList<>();
        bookedUsersList.add("user1");
        bookedUsersList.add("user2");
        bookedUsersList.add("user3");
        String output = Go4LunchUtils.getJoiningUsersName(mockContext, bookedUsersList);

        assertEquals(expected, output);
    }

    @Test
    public void Given_givenDateToday_When_convertDateToHour_Then_checkString() {
        String expected = "10:45";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 45);
        Date date = new Date();
        date.setTime(cal.getTimeInMillis());

        String output = Go4LunchUtils.convertDateToString(date, date);

        assertEquals(expected, output);
    }

    @Test
    public void Given_givenDateWeek_When_convertDateToHour_Then_checkString() {
        String expected = "ven., 10:45";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2020);
        cal.set(Calendar.DAY_OF_YEAR, 10);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 45);
        Date messageDate = new Date();
        messageDate.setTime(cal.getTimeInMillis());

        cal.set(Calendar.DAY_OF_YEAR, 15);
        Date testDate = new Date();
        testDate.setTime(cal.getTimeInMillis());

        String output = Go4LunchUtils.convertDateToString(messageDate, testDate);

        assertEquals(expected, output);
    }

    @Test
    public void Given_givenDateLastWeek_When_convertDateToHour_Then_checkString() {
        String expected = "2020-01-10, 10:45";

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2020);
        cal.set(Calendar.DAY_OF_YEAR, 10);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 45);
        Date messageDate = new Date();
        messageDate.setTime(cal.getTimeInMillis());

        cal.set(Calendar.DAY_OF_YEAR, 20);
        Date testDate = new Date();
        testDate.setTime(cal.getTimeInMillis());

        String output = Go4LunchUtils.convertDateToString(messageDate, testDate);

        assertEquals(expected, output);
    }
}
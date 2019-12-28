package org.desperu.go4lunch.utils;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.LocalTime;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;

import org.desperu.go4lunch.R;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static org.desperu.go4lunch.Go4LunchTools.OpeningHoursState.*;

public class Go4LunchUtils {

    private static String restaurantType;
    private static int openingHoursColor;

    /**
     * Get joining name string, for users eating in restaurant.
     * @param context Context from this method is called.
     * @param userName User name.
     * @return Joining name string.
     */
    @NotNull
    public static String getJoiningName(@NotNull Context context, String userName) {
        return context.getResources().getString(R.string.activity_restaurant_detail_recycler_text_joining, userName);
    }

    // --------------
    // RESTAURANT NAME ADDRESS AND TYPE
    // --------------

    /**
     * Get simple restaurant name string.
     * @param restaurantName Full restaurant name.
     * @return Simple restaurant name.
     */
    @NotNull
    public static String getSimpleRestaurantName(@NotNull String restaurantName) {
        restaurantType = null;
        List<String> str = Arrays.asList(restaurantName.split("-"));
        if (str.size() > 1) restaurantType = str.get(1);
        return str.get(0);
    }

    /**
     * Get  restaurant street address.
     * @param address Full restaurant address.
     * @return String restaurant street address.
     */
    public static String getRestaurantStreetAddress(@NotNull String address) {
        List<String> str = Arrays.asList(address.split(","));
        return str.get(0).length() > 2 ? str.get(0) : str.get(0) + str.get(1);
    }

    /**
     * Get restaurant type.
     * @param restaurantName Restaurant name.
     * @return String restaurant type.
     */
    @NotNull
    public static String getRestaurantType(String restaurantName) {
        if (restaurantType == null) getSimpleRestaurantName(restaurantName);
        else if (!restaurantType.isEmpty()) {
            if (restaurantType.toLowerCase().contains("restaurant")) {
                List<String> type = Arrays.asList(restaurantType.split(" "));
                return Character.toUpperCase(type.get(type.size() - 1).charAt(0)) + type.get(type.size() - 1).substring(1) + " - ";
            } else return restaurantType + " - ";
        }
        return "";
    }

    // --------------
    // OPENING HOURS
    // --------------

    /**
     * Get opening hours.
     * @param context Context from this method is called.
     * @param openingHours Restaurant opening hours.
     * @return Current opening hours string.
     */
    public static String getOpeningHours(Context context, OpeningHours openingHours) {
        if (openingHours != null) {
            Calendar cal = Calendar.getInstance();
            String period = openingHours.getWeekdayText().get(convertDayOfWeek(cal.get(Calendar.DAY_OF_WEEK)));
            List<String> periodSplit = Arrays.asList(period.split(": "));
            if (period.contains("/")) {
                openingHoursColor = OPEN;
                return periodSplit.get(periodSplit.size() - 1);
            }
            else if (period.contains("–")) return getTodayOpeningHours(context, openingHours, cal);
            else {
                openingHoursColor = CLOSE;
                return context.getString(R.string.go4lunch_utils_opening_hours_close);
            }
        }
        openingHoursColor = NO_DATA;
        return context.getString(R.string.go4lunch_utils_opening_hours_no_data);
    }

    /**
     * Get today opening hours.
     * @param openingHours Restaurant opening hours.
     * @param cal Calendar instance.
     * @return Current opening hours string.
     */
    @NotNull
    private static String getTodayOpeningHours(Context context, @NotNull OpeningHours openingHours, @NotNull Calendar cal) {
        List<Period> periodList = openingHours.getPeriods();

        // Get current time and date data
        int dayOfWeekNumber = cal.get(Calendar.DAY_OF_WEEK);
        LocalTime localTime = LocalTime.newInstance(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));

        // Search opening hours for current day
        for (int i = 0; i < periodList.size(); i++) {
            // Get opening hours for today
            if (periodList.get(i).getOpen().getDay().compareTo(getDayOfWeek(dayOfWeekNumber)) == 0) {
                // If current time >= open hour and, close hour > current time or close day > today
                if (localTime.compareTo(periodList.get(i).getOpen().getTime()) >= 0
                        && (periodList.get(i).getClose().getTime().compareTo(localTime) > 0
                        || periodList.get(i).getClose().getDay().compareTo(getDayOfWeek(dayOfWeekNumber)) < 0)) {
                    openingHoursColor = OPEN;
                    return context.getString(R.string.go4lunch_utils_opening_hours_open_until)
                            + setTimeFormat(periodList.get(i).getClose().getTime().getHours(),
                            periodList.get(i).getClose().getTime().getMinutes());
                    // Else if open hour > now
                } else if (periodList.get(i).getOpen().getTime().compareTo(localTime) > 0) {
                    openingHoursColor = OPEN_AT;
                    return context.getString(R.string.go4lunch_utils_opening_hours_open_at)
                            + setTimeFormat(periodList.get(i).getOpen().getTime().getHours(),
                            periodList.get(i).getOpen().getTime().getMinutes());
                }
            }
        }
        for (int i = 0; i < periodList.size(); i++) {
            // If close today, show open hour tomorrow
            cal.add(Calendar.DAY_OF_WEEK, 1);
            if (periodList.get(i).getOpen().getDay().compareTo(getDayOfWeek(cal.get(Calendar.DAY_OF_WEEK))) == 0) {
                openingHoursColor = OPEN_AT;
                String nextDay = periodList.get(i).getOpen().getDay().toString().toLowerCase();
                return context.getString(R.string.go4lunch_utils_opening_hours_next_open_hour,
                        Character.toUpperCase(nextDay.charAt(0)) + nextDay.substring(1))
                        + setTimeFormat(periodList.get(i).getOpen().getTime().getHours(),
                        periodList.get(i).getOpen().getTime().getMinutes());
            }
        }
        openingHoursColor = CLOSE;
        return context.getString(R.string.go4lunch_utils_opening_hours_close);
    }

    /**
     * Get DayOfWeek object for today.
     * @param dayOfWeekNumber Integer day of week number, from calendar.
     * @return Corresponding DayOfWeek object.
     */
    @Nullable
    @Contract(pure = true)
    private static DayOfWeek getDayOfWeek(int dayOfWeekNumber) {
        switch (dayOfWeekNumber) {
            case 1: return DayOfWeek.SUNDAY;
            case 2: return DayOfWeek.MONDAY;
            case 3: return DayOfWeek.TUESDAY;
            case 4: return DayOfWeek.WEDNESDAY;
            case 5: return DayOfWeek.THURSDAY;
            case 6: return DayOfWeek.FRIDAY;
            case 7: return DayOfWeek.SATURDAY;
            default: return null;
        }
    }

    /**
     * Convert DayOfWeek to correspond with opening hours.
     * @param dayOfWeekNumber Integer day of week number, from calendar.
     * @return Corresponding DayOfWeek int.
     */
    @Contract(pure = true)
    private static int convertDayOfWeek(int dayOfWeekNumber) {
        switch (dayOfWeekNumber) {
            case 1: return 6; // Sunday
            case 2: return 0; // Monday
            case 3: return 1; // Tuesday
            case 4: return 2; // Wednesday
            case 5: return 3; // Thursday
            case 6: return 4; // Friday
            case 7: return 5; // Saturday
            default: return -1;
        }
    }

    /**
     * Set time with good format.
     * @param hours Integer hours.
     * @param minutes Integer minutes.
     * @return String time with good format.
     */
    @NotNull
    private static String setTimeFormat(int hours, int minutes) {
        String language = Locale.getDefault().getDisplayLanguage();
        String strMinutes = minutes > 0 ? String.valueOf(minutes) : "";
        if (language.equals("en")) return hours > 12 ? hours - 12 + "." + strMinutes + "pm" : hours + "." + strMinutes + "am";
        else return hours + "h" + strMinutes;
    }

    /**
     * Get opening hours text color, depending of restaurant state.
     * @return Corresponding integer color.
     */
    @Contract(pure = true)
    public static int getOpeningHoursColor() {
        switch (openingHoursColor) {
            case OPEN: return R.color.colorOpen;
            case OPEN_AT: return R.color.colorOpenAt;
            case CLOSE: return R.color.colorClose;
            default: return R.color.colorLightDark;
        }
    }

    /**
     * Get restaurant status.
     * @return Is restaurant opened.
     */
    @Contract(pure = true)
    public static boolean getIsOpened() { return openingHoursColor != 2; }

    // --------------
    // RESTAURANT DISTANCE
    // --------------

    /**
     * Get distance between user position and restaurant position, in meters.
     * @param userPosition User position.
     * @param restaurantPosition Restaurant position.
     * @return String distance in meters.
     */
    @NotNull
    public static String getRestaurantDistance(@NotNull LatLng userPosition, @NotNull LatLng restaurantPosition) {
        float[] results = new float[1];
        if (userPosition != null && restaurantPosition != null) {
            Location.distanceBetween(userPosition.latitude, userPosition.longitude,
                    restaurantPosition.latitude, restaurantPosition.longitude, results);
            return ((int) results[0]) + "m";
        }
        return "no data";
    }
}
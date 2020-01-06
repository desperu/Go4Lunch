package org.desperu.go4lunch.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Objects;

public class NotificationAlarmManager {

    /**
     * Get pending intent for alarm manager, to call broadcast receiver at alarm time.
     * @param context Context from this method is called.
     * @return Created pending intent.
     */
    private static PendingIntent getPendingIntent(Context context) {
        Intent alarmIntent = new Intent(context, NotificationReceiver.class);
        return PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
    }

    /**
     * Get alarm time in millis.
     * @return Alarm time in millis.
     */
    private static long getAlarmTime() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 12);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * Enable notification alarm.
     * @param context Context from this method is called.
     */
    public static void startNotificationsAlarm(@NotNull Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Objects.requireNonNull(manager).setRepeating(AlarmManager.RTC_WAKEUP,
                getAlarmTime(), AlarmManager.INTERVAL_DAY, getPendingIntent(context));
    }

    /**
     * Disable notification alarm.
     * @param context Context from this method is called.
     */
    public static void stopNotificationsAlarm(@NotNull Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Objects.requireNonNull(manager).cancel(getPendingIntent(context));
    }
}
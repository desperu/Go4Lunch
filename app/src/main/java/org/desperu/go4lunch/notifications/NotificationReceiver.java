package org.desperu.go4lunch.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import com.google.firebase.auth.FirebaseAuth;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.api.firestore.RestaurantHelper;
import org.desperu.go4lunch.api.firestore.UserHelper;
import org.desperu.go4lunch.models.Restaurant;
import org.desperu.go4lunch.models.User;
import org.desperu.go4lunch.utils.Go4LunchPrefs;
import org.desperu.go4lunch.utils.Go4LunchUtils;
import org.desperu.go4lunch.view.main.MainActivity;
import org.desperu.go4lunch.view.restaurantdetail.RestaurantDetailActivity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import static org.desperu.go4lunch.Go4LunchTools.PrefsKeys.NOTIFICATION_ENABLED;
import static org.desperu.go4lunch.Go4LunchTools.SettingsDefault.NOTIFICATION_DEFAULT;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "Go4LunchNotification";
    private static final String NOTIFICATION_NAME = "JoiningWorkmates";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_DESCRIPTION = "Go4lunch notifications channel";

    private Context context;
    private Restaurant bookedRestaurant;

    @Override
    public void onReceive(Context context, @NotNull Intent intent) {
        this.context = context;
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Toast.makeText(context, "boot completed", Toast.LENGTH_SHORT).show(); // TODO to remove, for test
            if (Go4LunchPrefs.getBoolean(context, NOTIFICATION_ENABLED, NOTIFICATION_DEFAULT))
                NotificationAlarmManager.startNotificationsAlarm(context);
        }
        else if (checkTime()) getBookedRestaurantId();
    }

    /**
     * Create notification, and set on click.
     * @param bookedUserNameList Booked user name list.
     */
    private void createNotification(@NotNull List<String> bookedUserNameList) {
        // Create notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_base_logo_black)
                .setContentTitle(context.getString(R.string.notification_title, Go4LunchUtils.getSimpleRestaurantName(bookedRestaurant.getName())))
                .setContentText(bookedUserNameList.size() == 0 ?
                        context.getString(R.string.notification_text_no_joining_user) :
                        context.getString(R.string.notification_text) + Go4LunchUtils.getJoiningUsersName(context, bookedUserNameList))
                .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                .setAutoCancel(true);

        // Create intent for notification click.
        Intent resultIntent = new Intent(context, RestaurantDetailActivity.class)
                .putExtra(RestaurantDetailActivity.RESTAURANT_ID, bookedRestaurant.getRestaurantId());

        // Add parent and activity to the top of stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        // Adds the intent that starts the activity.
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        // Notification Manager instance.
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);

        // Support Version >= Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationManagerCompat.createNotificationChannel(mChannel);
        }

        // Show notification.
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Check that current time is lower than 12.15am.
     * @return If current time is lower than 12.15am.
     */
    private boolean checkTime() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.HOUR_OF_DAY) <= 12 && cal.get(Calendar.MINUTE) <= 15;
    }

    /**
     * Get current user id.
     * @return Current user id.
     */
    @NotNull
    private String getUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }

    /**
     * Get booked restaurant id.
     */
    private void getBookedRestaurantId() {
        UserHelper.getUser(getUserId()).addOnSuccessListener(documentSnapshot ->
                getBookedRestaurant(Objects.requireNonNull(
                        documentSnapshot.toObject(User.class)).getBookedRestaurantId()));
    }

    /**
     * Get booked restaurant object.
     * @param restaurantId Booked restaurant id.
     */
    private void getBookedRestaurant(String restaurantId) {
        if (restaurantId != null && !restaurantId.isEmpty())
            RestaurantHelper.getRestaurant(restaurantId).addOnSuccessListener(documentSnapshot -> {
                bookedRestaurant = documentSnapshot.toObject(Restaurant.class);
                removeCurrentUserIdFromList(Objects.requireNonNull(bookedRestaurant).getBookedUsersId());
            });
    }

    /**
     * Remove current user id from booked users list.
     * @param bookedUserIdList Booked user id list for restaurant.
     */
    private void removeCurrentUserIdFromList(@NotNull List<String> bookedUserIdList) {
        for (String userId : bookedUserIdList)
            if (userId.equals(getUserId())) bookedUserIdList.remove(userId);
        getBookedUserNameList(bookedUserIdList);
    }

    /**
     * Get booked user name list for restaurant.
     * @param bookedUserIdList Booked user id list.
     */
    private void getBookedUserNameList(@NotNull List<String> bookedUserIdList) {
        List<String> bookedUserNameList = new ArrayList<>();
        for (int i = 0; i < bookedUserIdList.size(); i++) {
            UserHelper.getUser(bookedUserIdList.get(i)).addOnSuccessListener(documentSnapshot -> {
                bookedUserNameList.add(Objects.requireNonNull(documentSnapshot.toObject(User.class)).getUserName());
                if (bookedUserNameList.size() == bookedUserIdList.size()) createNotification(bookedUserNameList);
            });
        }
        if (bookedUserIdList.size() == 0) createNotification(bookedUserNameList);
    }
}
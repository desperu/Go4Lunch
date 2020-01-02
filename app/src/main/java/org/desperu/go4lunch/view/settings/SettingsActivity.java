package org.desperu.go4lunch.view.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.desperu.go4lunch.R;
import org.desperu.go4lunch.utils.Go4LunchPrefs;
import org.desperu.go4lunch.view.base.BaseActivity;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.OnClick;

import static org.desperu.go4lunch.Go4LunchTools.PrefsKeys.*;
import static org.desperu.go4lunch.Go4LunchTools.SettingsDefault.*;

public class SettingsActivity extends BaseActivity {

    // FOR DESIGN
    @BindView(R.id.activity_settings_relative_root) RelativeLayout relativeRoot;
    @BindView(R.id.activity_settings_notification_switch) Switch notificationSwitch;
    @BindView(R.id.activity_settings_notification_reset_booked_restaurant_switch) Switch resetBookedRestaurantSwitch;
    @BindView(R.id.activity_settings_text_map_zoom_level_value) TextView zoomLevelTextView;
    @BindView(R.id.activity_settings_map_zoom_button_switch) Switch zoomButtonSwitch;
    @BindView(R.id.activity_settings_map_auto_refresh_location_switch) Switch refreshLocationSwitch;
    @Nullable @BindView(R.id.alert_dialog_linear_root) LinearLayout linearRoot;

    // FOR DATA
    private boolean isNotificationsEnabled;
    private boolean isResetBookedRestaurantEnabled;
    private int zoomLevel;
    private boolean isZoomButtonEnabled;
    private boolean isRefreshLocationEnabled;
    private static final int ZOOM_DIALOG = 0;
    private static final int RESET_DIALOG = 1;

    // --------------
    // BASE METHODS
    // --------------

    @Override
    protected int getActivityLayout() { return R.layout.activity_settings; }

    @Override
    protected void configureDesign() {
        this.configureToolBar();
        this.configureUpButton();
        this.getSavedPrefs();
        this.updateUiWithSavedPrefs();
    }

    // --------------
    // METHODS OVERRIDE
    // --------------

    @Override
    protected void onPause() {
        super.onPause();
        this.savePrefs();
    }

    // --------------
    // CONFIGURATION
    // --------------

    /**
     * Get saved prefs.
     */
    private void getSavedPrefs() {
        isNotificationsEnabled = Go4LunchPrefs.getBoolean(getBaseContext(), NOTIFICATION_ENABLED, NOTIFICATION_DEFAULT);
        isResetBookedRestaurantEnabled = Go4LunchPrefs.getBoolean(getBaseContext(), RESET_BOOKED_RESTAURANT, RESET_BOOKED_DEFAULT);
        zoomLevel = Go4LunchPrefs.getInt(getBaseContext(), MAP_ZOOM_LEVEL, ZOOM_LEVEL_DEFAULT);
        isZoomButtonEnabled = Go4LunchPrefs.getBoolean(getBaseContext(), MAP_ZOOM_BUTTON, ZOOM_BUTTON_DEFAULT);
        isRefreshLocationEnabled = Go4LunchPrefs.getBoolean(getBaseContext(), MAP_AUTO_REFRESH_LOCATION, AUTO_REFRESH_DEFAULT);
    }

    /**
     * Save current prefs.
     */
    private void savePrefs() { // TODO save date for reset booked restaurant
        Go4LunchPrefs.savePref(getBaseContext(), NOTIFICATION_ENABLED, notificationSwitch.isChecked());
        Go4LunchPrefs.savePref(getBaseContext(), RESET_BOOKED_RESTAURANT, resetBookedRestaurantSwitch.isChecked());
        Go4LunchPrefs.savePref(getBaseContext(), MAP_ZOOM_LEVEL, Integer.parseInt(zoomLevelTextView.getText().toString()));
        Go4LunchPrefs.savePref(getBaseContext(), MAP_ZOOM_BUTTON, zoomButtonSwitch.isChecked());
        Go4LunchPrefs.savePref(getBaseContext(), MAP_AUTO_REFRESH_LOCATION, refreshLocationSwitch.isChecked());
    }

    // --------------
    // ACTION
    // --------------

    @OnClick(R.id.activity_settings_map_zoom_level_container)
    protected void onClickZoomSize() { this.alertDialog(ZOOM_DIALOG); }

    @OnClick(R.id.activity_settings_reset_settings_container)
    protected void onclickResetSettings() { this.alertDialog(RESET_DIALOG); }

    // --------------
    // UI
    // --------------

    /**
     * Update ui with saved prefs data.
     */
    private void updateUiWithSavedPrefs() {
        notificationSwitch.setChecked(isNotificationsEnabled);
        resetBookedRestaurantSwitch.setChecked(isResetBookedRestaurantEnabled);
        zoomLevelTextView.setText(String.valueOf(zoomLevel));
        zoomButtonSwitch.setChecked(isZoomButtonEnabled);
        refreshLocationSwitch.setChecked(isRefreshLocationEnabled);
    }

    /**
     * Create alert dialog to set zoom value or confirm reset settings.
     * @param zoomOrReset Key to show corresponding dialog.
     */
    private void alertDialog(int zoomOrReset) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        if (zoomOrReset == ZOOM_DIALOG) {
            dialog.setTitle(R.string.activity_settings_text_map_zoom_size);
            dialog.setMessage(R.string.activity_settings_text_map_zoom_size_description);

            View editView = LayoutInflater.from(this).inflate(R.layout.alert_dialog, linearRoot);
            final EditText editText = editView.findViewById(R.id.alert_dialog_input_text);
            editText.setText(String.valueOf(zoomLevel));
            dialog.setView(editView);

            dialog.setPositiveButton(R.string.activity_settings_dialog_positive_button, (dialog1, which) -> {
                String newZoomLevel = editText.getText().toString();
                if (!newZoomLevel.isEmpty() && Integer.parseInt(newZoomLevel) >= 2 && Integer.parseInt(newZoomLevel) <= 21)
                    this.zoomLevelTextView.setText(newZoomLevel);
                else Toast.makeText(this, R.string.activity_settings_toast_zoom_level_wrong_value, Toast.LENGTH_LONG).show();
            });
        } else if (zoomOrReset == RESET_DIALOG) {
            dialog.setTitle(R.string.activity_settings_text_reset_settings);
            dialog.setMessage(R.string.activity_settings_dialog_reset_settings_message);

            dialog.setPositiveButton(R.string.activity_settings_dialog_positive_button, (dialog2, which) -> this.resetSettings());
        }
        dialog.setNegativeButton(R.string.activity_settings_dialog_negative_button, (dialog3, which) -> dialog3.cancel());
        dialog.show();
    }

    // --------------
    // UTILS
    // --------------

    /**
     * Reset settings to default value.
     */
    private void resetSettings() {
        notificationSwitch.setChecked(NOTIFICATION_DEFAULT);
        resetBookedRestaurantSwitch.setChecked(RESET_BOOKED_DEFAULT);
        zoomLevelTextView.setText(String.valueOf(ZOOM_LEVEL_DEFAULT));
        zoomButtonSwitch.setChecked(ZOOM_BUTTON_DEFAULT);
        refreshLocationSwitch.setChecked(AUTO_REFRESH_DEFAULT);
        Toast.makeText(getBaseContext(), R.string.activity_settings_toast_reset_settings_default, Toast.LENGTH_SHORT).show();
    }
}
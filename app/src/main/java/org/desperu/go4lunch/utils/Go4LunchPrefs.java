package org.desperu.go4lunch.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Manage shared preferences class.
 */
public class Go4LunchPrefs {

    private static final String SHARED_PREFS_FILE_NAME = "Go4LunchPrefs";

    /**
     * Initialize shared preferences.
     *
     * @param context Context from MyPref is called.
     */
    private static SharedPreferences getPrefs(@NotNull Context context) {
        return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save string value in shared preferences file.
     *
     * @param context Context from MyPref is called.
     * @param key     Key for the value.
     * @param value   String value to save.
     */
    public static void savePref(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    /**
     * Get string value from shared preferences file.
     *
     * @param context  Context from MyPref is called.
     * @param key      Key of the value.
     * @param defValue Default value.
     * @return The corresponding value of the key.
     */
    @Contract("_, _, !null -> !null")
    public static String getString(Context context, String key, String defValue) {
        return getPrefs(context).getString(key, defValue);
    }

    /**
     * Save integer value in shared preferences file.
     *
     * @param context Context from MyPref is called.
     * @param key     Key for the value.
     * @param value   Integer value to save.
     */
    public static void savePref(Context context, String key, int value) {
        getPrefs(context).edit().putInt(key, value).apply();
    }

    /**
     * Get integer value from shared preferences file.
     *
     * @param context  Context from MyPref is called.
     * @param key      Key of the value.
     * @param defValue Default value.
     * @return The corresponding value of the key.
     */
    public static int getInt(Context context, String key, int defValue) {
        return getPrefs(context).getInt(key, defValue);
    }

    /**
     * Save long value in shared preferences file.
     *
     * @param context Context from MyPref is called.
     * @param key     Key for the value.
     * @param value   Long value to save.
     */
    public static void savePref(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    /**
     * Get long value from shared preferences file.
     *
     * @param context  Context from MyPref is called.
     * @param key      Key of the value.
     * @param defValue Default value.
     * @return The corresponding value of the key.
     */
    public static long getLong(Context context, String key, long defValue) {
        return getPrefs(context).getLong(key, defValue);
    }

    /**
     * Save boolean value in shared preferences file.
     *
     * @param context Context from MyPref is called.
     * @param key     Key for the value.
     * @param value   The boolean value to save.
     */
    public static void savePref(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    /**
     * Get boolean value from shared preferences file.
     *
     * @param context  Context from MyPref is called.
     * @param key      Key of the value.
     * @param defValue Default value.
     * @return The corresponding value of the key.
     */
    public static boolean getBoolean(Context context, String key, boolean defValue) {
        return getPrefs(context).getBoolean(key, defValue);
    }

    /**
     * Clear a specific value in shared preferences.
     *
     * @param context Context from MyPref is called.
     * @param key     Key of the value.
     */
    public static void clear(Context context, String key) {
        getPrefs(context).edit().remove(key).apply();
    }
}
package com.atni.droid.cartrak;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Collections;
import java.util.Set;

public class PreferencesHelper {

    private static final String DEFAULT_STRING_VALUE = "";
    private static final Set<String> DEFAULT_SET_VALUE = Collections.EMPTY_SET;
    private static final double DEFAULT_DOUBLE_VALUE = 0d;
    private static final long DEFAULT_LONG_VALUE = 0l;
    private static final boolean DEFAULT_BOOLEAN_VALUE = false;

    public static final String CARTRAK_SHAREDPREFS = "cartrak.sharedprefs";

    public static final String SP_LOCATION_RESET = "cartrak.location.reset";
    public static final String SP_LOCATION_LATITUDE = "cartrak.location.latitude";
    public static final String SP_LOCATION_LONGTITUDE = "cartrak.location.longtitude";
    public static final String SP_LOCATION_ALTITUDE = "cartrak.location.altitude";
    public static final String SP_LOCATION_TIME = "cartrak.location.time";
    public static final String SP_BLUETOOTH_DEVICES_ENABLED = "cartrak.bluetoothdevices.enabled";



    /**
     * Returns Editor to modify values of SharedPreferences
     *
     * @param context Application context
     * @return editor instance
     */
    private static Editor getEditor(Context context) {
        return getPreferences(context).edit();
    }

    /**
     * Returns SharedPreferences object
     *
     * @param context Application context
     * @return shared preferences instance
     */
    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(CARTRAK_SHAREDPREFS,
                Context.MODE_PRIVATE);
    }

    public static void registerOnSharedPreferenceChangeListener(
                SharedPreferences.OnSharedPreferenceChangeListener listener, Context context) {
        getPreferences(context).registerOnSharedPreferenceChangeListener(listener);
    }

    public static void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener, Context context) {
        getPreferences(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    public static boolean contains(String key, Context context) {
        return getPreferences(context).contains(key);
    }


    public static void putStringSet(String tag, Set<String> value, Context context) {
        Editor editor = getEditor(context);
        editor.putStringSet(tag, value);
        editor.apply();
    }

    public static Set<String> getStringSet(String tag, Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        return sharedPreferences.getStringSet(tag, DEFAULT_SET_VALUE);
    }

    /**
     * Save a string on SharedPreferences
     *
     * @param tag     tag
     * @param value   value
     * @param context Application context
     */
    public static void putString(String tag, String value, Context context) {
        Editor editor = getEditor(context);
        editor.putString(tag, value);
        editor.apply();
    }

    /**
     * Get a string value from SharedPreferences
     *
     * @param tag     tag
     * @param context Application context
     * @return String value
     */
    public static String getString(String tag, Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        return sharedPreferences.getString(tag, DEFAULT_STRING_VALUE);
    }

    public static double getDouble(final String key, Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        return Double.longBitsToDouble(sharedPreferences.getLong(key, Double.doubleToLongBits(DEFAULT_DOUBLE_VALUE)));
    }

    public static void putDouble(final String key, final double value, Context context) {
        Editor editor = getEditor(context);
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.apply();
    }

    public static long getLong(String tag, Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        return sharedPreferences.getLong(tag, DEFAULT_LONG_VALUE);
    }

    public static void putLong(String tag, long value, Context context) {
        Editor editor = getEditor(context);
        editor.putLong(tag, value);
        editor.apply();
    }

    public static boolean getBoolean(String tag, Context context) {
        SharedPreferences sharedPreferences = getPreferences(context);
        return sharedPreferences.getBoolean(tag, DEFAULT_BOOLEAN_VALUE);
    }

    public static void putBoolean(String tag, boolean value, Context context) {
        Editor editor = getEditor(context);
        editor.putBoolean(tag, value);
        editor.apply();
    }

}

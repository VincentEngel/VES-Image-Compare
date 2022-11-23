package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyValueStorage {
    public static final String ASKED_FOR_REVIEW = "ASKED_FOR_REVIEW";
    public static final String USER_THEME = "USER_THEME";
    public static boolean getBoolean(Context context, String key, boolean defaultValue)
    {
        try {
            return context
                    .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .getBoolean(key, defaultValue);
        } catch (Exception ignored) {}

        return defaultValue;
    }

    public static void setBoolean(Context context, String key, boolean value)
    {
        try {
            SharedPreferences.Editor editor = context
                    .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit();
            editor.putBoolean(key, value);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public static String getString(Context context, String key, String defaultValue)
    {
        try {
            return context
                    .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .getString(key, defaultValue);
        } catch (Exception ignored) {}

        return defaultValue;
    }

    public static void setString(Context context, String key, String value)
    {
        try {
            SharedPreferences.Editor editor = context
                    .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public static void putInt(Context context, String key, int value)
    {
        try {
            SharedPreferences.Editor editor = context
                    .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .edit();
            editor.putInt(key, value);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public static int getInt(Context context, String key, int defaultValue)
    {
        try {
            return context
                    .getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                    .getInt(key, defaultValue);
        } catch (Exception ignored) {}

        return defaultValue;
    }
}

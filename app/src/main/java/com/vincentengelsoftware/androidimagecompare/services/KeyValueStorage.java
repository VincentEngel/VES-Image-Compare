package com.vincentengelsoftware.androidimagecompare.services;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyValueStorage {
    public static final String ASKED_FOR_REVIEW = "ASKED_FOR_REVIEW";
    public static final String USER_THEME = "USER_THEME";
    public static final String SYNCED_ZOOM = "SYNCED_ZOOM";
    public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";

    public static final String LAST_COMPARE_MODE = "LAST_COMPARE_MODE";

    public static final String LEFT_RESIZE = "LEFT_RESIZE";
    public static final String RIGHT_RESIZE = "RIGHT_RESIZE";

    private final Context context;

    public KeyValueStorage(Context context) {
        this.context = context;
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    private SharedPreferences.Editor getEditor() {
        return this.getSharedPreferences().edit();
    }

    public boolean getBoolean(String key, boolean defaultValue)
    {
        try {
            return this.getSharedPreferences().getBoolean(key, defaultValue);
        } catch (Exception ignored) {}

        return defaultValue;
    }

    public void setBoolean(String key, boolean value)
    {
        try {
            SharedPreferences.Editor editor = this.getEditor();
            editor.putBoolean(key, value);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public String getString(String key, String defaultValue)
    {
        try {
            return this.getSharedPreferences().getString(key, defaultValue);
        } catch (Exception ignored) {}

        return defaultValue;
    }

    public void setString(String key, String value)
    {
        try {
            SharedPreferences.Editor editor = this.getEditor();
            editor.putString(key, value);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public void putInt(String key, int value)
    {
        try {
            SharedPreferences.Editor editor = this.getEditor();
            editor.putInt(key, value);
            editor.apply();
        } catch (Exception ignored) {}
    }

    public int getInt(String key, int defaultValue)
    {
        try {
            return this.getSharedPreferences().getInt(key, defaultValue);
        } catch (Exception ignored) {}

        return defaultValue;
    }
}

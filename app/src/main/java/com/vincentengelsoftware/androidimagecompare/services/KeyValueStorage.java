package com.vincentengelsoftware.androidimagecompare.services;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyValueStorage {
    public static final String ASKED_FOR_REVIEW = "ASKED_FOR_REVIEW";

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

    public void remove(String key)
    {
        try {
            SharedPreferences.Editor editor = this.getEditor();
            editor.remove(key);
            editor.apply();
        } catch (Exception ignored) {}
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

    public void setInt(String key, int value)
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

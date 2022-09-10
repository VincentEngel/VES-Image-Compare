package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class KeyValueStorage {
    public static final String ASKED_FOR_REVIEW = "ASKED_FOR_REVIEW";
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
}

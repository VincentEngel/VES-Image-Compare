package com.vincentengelsoftware.androidimagecompare.ui.util;

import androidx.appcompat.app.AppCompatDelegate;

import com.vincentengelsoftware.androidimagecompare.constants.Status;

public class Theme {
    public static void updateTheme(int theme)
    {
        if (theme == Status.THEME_SYSTEM) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            return;
        }

        if (theme == Status.THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}

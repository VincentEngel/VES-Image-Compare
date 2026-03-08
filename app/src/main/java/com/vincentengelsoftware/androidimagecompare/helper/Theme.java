package com.vincentengelsoftware.androidimagecompare.helper;

import android.widget.Button;

import androidx.appcompat.app.AppCompatDelegate;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Status;

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

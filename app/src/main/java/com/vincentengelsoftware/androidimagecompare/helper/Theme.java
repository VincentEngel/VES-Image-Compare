package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.Button;

import androidx.appcompat.app.AppCompatDelegate;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Status;

public class Theme {
    public static int getCurrentTheme(Resources resources)
    {
        int currentNightMode = resources.getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_NO) {
            return Status.THEME_LIGHT;
        } else {
            return Status.THEME_DARK;
        }
    }

    public static void updateTheme(int theme, int currentTheme)
    {
        if (theme == currentTheme) {
            return;
        }

        if (theme == Status.THEME_LIGHT) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }

    public static int map(int theme)
    {
        if (theme == Status.THEME_SYSTEM) {
            return Status.THEME_DEFAULT_SYSTEM;
        }

        return theme;
    }

    public static void updateButtonText(Button button, int theme)
    {
        switch (theme) {
            case Status.THEME_SYSTEM:
                button.setText(R.string.system);
                break;
            case Status.THEME_LIGHT:
                button.setText(R.string.light);
                break;
            case Status.THEME_DARK:
            default:
                button.setText(R.string.dark);
        }
    }
}

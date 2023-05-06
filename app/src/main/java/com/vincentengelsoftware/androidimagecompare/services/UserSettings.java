package com.vincentengelsoftware.androidimagecompare.services;

import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.globals.Status;

public class UserSettings {
    private final KeyValueStorage keyValueStorage;

    public static final String USER_THEME = "USER_THEME";
    public static final String SYNCED_ZOOM = "SYNCED_ZOOM";
    public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";

    public static final String LAST_COMPARE_MODE = "LAST_COMPARE_MODE";

    public static final String RESIZE_LEFT_IMAGE = "LEFT_RESIZE";
    public static final String RESIZE_RIGHT_IMAGE = "RIGHT_RESIZE";

    public UserSettings(KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;
    }

    public String getLastCompareMode() {
        return this.keyValueStorage.getString(UserSettings.LAST_COMPARE_MODE, CompareModeNames.SIDE_BY_SIDE);
    }

    public void setLastCompareMode(String lastCompareMode) {
        this.keyValueStorage.setString(UserSettings.LAST_COMPARE_MODE, lastCompareMode);
    }

    public boolean isSyncedZoom() {
        return this.keyValueStorage.getBoolean(UserSettings.SYNCED_ZOOM, true);
    }

    public void setSyncedZoom(boolean syncedZoom) {
        this.keyValueStorage.setBoolean(UserSettings.SYNCED_ZOOM, syncedZoom);
    }

    public boolean isShowExtensions() {
        return this.keyValueStorage.getBoolean(UserSettings.SHOW_EXTENSIONS, true);
    }

    public void setShowExtensions(boolean showExtensions) {
        this.keyValueStorage.setBoolean(UserSettings.SHOW_EXTENSIONS, showExtensions);
    }

    public boolean isResizeLeftImage() {
        return this.keyValueStorage.getBoolean(UserSettings.RESIZE_LEFT_IMAGE, true);
    }

    public void setResizeLeftImage(boolean resizeLeftImage) {
        this.keyValueStorage.setBoolean(UserSettings.RESIZE_LEFT_IMAGE, resizeLeftImage);
    }

    public boolean isResizeRightImage() {
        return this.keyValueStorage.getBoolean(UserSettings.RESIZE_RIGHT_IMAGE, true);
    }

    public void setResizeRightImage(boolean resizeRightImage) {
        this.keyValueStorage.setBoolean(UserSettings.RESIZE_RIGHT_IMAGE, resizeRightImage);
    }

    public int getTheme() {
        return this.keyValueStorage.getInt(UserSettings.USER_THEME, Status.THEME_SYSTEM);
    }

    public void setTheme(int theme) {
        this.keyValueStorage.putInt(UserSettings.USER_THEME, theme);
    }
}

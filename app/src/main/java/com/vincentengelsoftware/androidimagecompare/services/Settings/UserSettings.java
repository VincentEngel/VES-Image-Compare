package com.vincentengelsoftware.androidimagecompare.services.Settings;

import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;

public class UserSettings {
    private static UserSettings instance;
    private final KeyValueStorage keyValueStorage;
    private String lastCompareMode;
    private boolean syncedZoom;
    private boolean showExtensions;
    private int theme;

    private int maxZoom;

    public static final String USER_THEME = "USER_THEME";
    public static final String SYNCED_ZOOM = "SYNCED_ZOOM";
    public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";
    public static final String LAST_COMPARE_MODE = "LAST_COMPARE_MODE";
    public static final String MAX_ZOOM = "MAX_ZOOM";

    private final ImageResizeSettings LeftImageResizeSettings;
    private final ImageResizeSettings RightImageResizeSettings;

    private UserSettings(KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;

        this.lastCompareMode = this.keyValueStorage.getString(UserSettings.LAST_COMPARE_MODE, DefaultSettings.COMPARE_MODE);
        this.syncedZoom = this.keyValueStorage.getBoolean(UserSettings.SYNCED_ZOOM, DefaultSettings.SYNCED_ZOOM);
        this.showExtensions = this.keyValueStorage.getBoolean(UserSettings.SHOW_EXTENSIONS, DefaultSettings.SHOW_EXTENSIONS);
        this.theme = this.keyValueStorage.getInt(UserSettings.USER_THEME, DefaultSettings.THEME);

        this.maxZoom = this.keyValueStorage.getInt(UserSettings.MAX_ZOOM, DefaultSettings.MAX_ZOOM);

        this.LeftImageResizeSettings = new ImageResizeSettings("LEFT_", this.keyValueStorage);
        this.RightImageResizeSettings = new ImageResizeSettings("RIGHT_", this.keyValueStorage);
    }

    public static UserSettings getInstance(KeyValueStorage keyValueStorage) {
        if (instance == null) {
            instance = new UserSettings(keyValueStorage);
        }

        return instance;
    }

    public ImageResizeSettings getLeftImageResizeSettings() {
        return this.LeftImageResizeSettings;
    }

    public ImageResizeSettings getRightImageResizeSettings() {
        return this.RightImageResizeSettings;
    }

    public String getLastCompareMode() {
        return this.lastCompareMode;
    }

    public void setLastCompareMode(String lastCompareMode) {
        if (this.lastCompareMode.equals(lastCompareMode)) {
            return;
        }

        this.lastCompareMode = lastCompareMode;
        this.keyValueStorage.setString(UserSettings.LAST_COMPARE_MODE, lastCompareMode);
    }

    public boolean isSyncedZoom() {
        return this.syncedZoom;
    }

    public void setSyncedZoom(boolean syncedZoom) {
        if (this.syncedZoom == syncedZoom) {
            return;
        }

        this.syncedZoom = syncedZoom;
        this.keyValueStorage.setBoolean(UserSettings.SYNCED_ZOOM, syncedZoom);
    }

    public boolean isShowExtensions() {
        return this.showExtensions;
    }

    public void setShowExtensions(boolean showExtensions) {
        if (this.showExtensions == showExtensions) {
            return;
        }

        this.showExtensions = showExtensions;
        this.keyValueStorage.setBoolean(UserSettings.SHOW_EXTENSIONS, showExtensions);
    }

    public int getTheme() {
        return this.theme;
    }

    public void setTheme(int theme) {
        if (this.theme == theme) {
            return;
        }

        this.theme = theme;
        this.keyValueStorage.setInt(UserSettings.USER_THEME, theme);
    }

    public int getMaxZoom() {
        return this.maxZoom;
    }

    public void setMaxZoom(int maxZoom) {
        if (this.maxZoom == maxZoom) {
            return;
        }

        if (maxZoom < 1) {
            maxZoom = 1;
        }

        this.maxZoom = maxZoom;
        Settings.MAX_ZOOM = maxZoom;
        this.keyValueStorage.setInt(UserSettings.MAX_ZOOM, maxZoom);
    }
}

package com.vincentengelsoftware.androidimagecompare.services;

import com.vincentengelsoftware.androidimagecompare.Activities.CompareModes.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.globals.Status;

public class UserSettings {
    private static UserSettings instance;
    private final KeyValueStorage keyValueStorage;
    private String lastCompareMode;
    private boolean syncedZoom;
    private boolean showExtensions;
    private boolean resizeLeftImage;
    private boolean resizeRightImage;
    private int theme;

    private int maxZoom;

    public static final String USER_THEME = "USER_THEME";
    public static final String SYNCED_ZOOM = "SYNCED_ZOOM";
    public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";
    public static final String LAST_COMPARE_MODE = "LAST_COMPARE_MODE";
    public static final String RESIZE_LEFT_IMAGE = "LEFT_RESIZE";
    public static final String RESIZE_RIGHT_IMAGE = "RIGHT_RESIZE";

    public static final String MAX_ZOOM = "MAX_ZOOM";

    private final ImageResizeSettings LeftImageResizeSettings;
    private final ImageResizeSettings RightImageResizeSettings;

    private UserSettings(KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;

        this.lastCompareMode = this.keyValueStorage.getString(UserSettings.LAST_COMPARE_MODE, CompareModeNames.SIDE_BY_SIDE);
        this.syncedZoom = this.keyValueStorage.getBoolean(UserSettings.SYNCED_ZOOM, true);
        this.showExtensions = this.keyValueStorage.getBoolean(UserSettings.SHOW_EXTENSIONS, true);
        this.resizeLeftImage = this.keyValueStorage.getBoolean(UserSettings.RESIZE_LEFT_IMAGE, true);
        this.resizeRightImage = this.keyValueStorage.getBoolean(UserSettings.RESIZE_RIGHT_IMAGE, true);
        this.theme = this.keyValueStorage.getInt(UserSettings.USER_THEME, Status.THEME_SYSTEM);

        this.maxZoom = this.keyValueStorage.getInt(UserSettings.MAX_ZOOM, 10);

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

    public boolean isResizeLeftImage() {
        return this.resizeLeftImage;
    }

    public void setResizeLeftImage(boolean resizeLeftImage) {
        if (this.resizeLeftImage == resizeLeftImage) {
            return;
        }

        this.resizeLeftImage = resizeLeftImage;
        this.keyValueStorage.setBoolean(UserSettings.RESIZE_LEFT_IMAGE, resizeLeftImage);
    }

    public boolean isResizeRightImage() {
        return this.resizeRightImage;
    }

    public void setResizeRightImage(boolean resizeRightImage) {
        if (this.resizeRightImage == resizeRightImage) {
            return;
        }

        this.resizeRightImage = resizeRightImage;
        this.keyValueStorage.setBoolean(UserSettings.RESIZE_RIGHT_IMAGE, resizeRightImage);
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
        this.keyValueStorage.setInt(UserSettings.MAX_ZOOM, maxZoom);
    }
}

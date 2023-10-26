package com.vincentengelsoftware.androidimagecompare.services.Settings;

import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;

public class UserSettings {
    private static UserSettings instance;
    private final KeyValueStorage keyValueStorage;
    public static final String USER_THEME = "USER_THEME";
    public static final String SYNCED_ZOOM = "SYNCED_ZOOM";
    public static final String SHOW_EXTENSIONS = "SHOW_EXTENSIONS";
    public static final String LAST_COMPARE_MODE = "LAST_COMPARE_MODE";
    public static final String MAX_ZOOM = "MAX_ZOOM";

    public static final String RESET_IMAGE_ON_LINKING = "RESET_IMAGE_ON_LINKING";

    public static final String MIRRORING_TYPE = "MIRRORING_TYPE";

    private final ImageResizeSettings LeftImageResizeSettings;
    private final ImageResizeSettings RightImageResizeSettings;

    private UserSettings(KeyValueStorage keyValueStorage) {
        this.keyValueStorage = keyValueStorage;

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
        return this.keyValueStorage.getString(UserSettings.LAST_COMPARE_MODE, DefaultSettings.COMPARE_MODE);
    }

    public void setLastCompareMode(String lastCompareMode) {
        this.keyValueStorage.setString(UserSettings.LAST_COMPARE_MODE, lastCompareMode);
    }

    public boolean isSyncedZoom() {
        return this.keyValueStorage.getBoolean(UserSettings.SYNCED_ZOOM, DefaultSettings.SYNCED_ZOOM);
    }

    public void setSyncedZoom(boolean syncedZoom) {
        this.keyValueStorage.setBoolean(UserSettings.SYNCED_ZOOM, syncedZoom);
    }

    public boolean isShowExtensions() {
        return this.keyValueStorage.getBoolean(UserSettings.SHOW_EXTENSIONS, DefaultSettings.SHOW_EXTENSIONS);
    }

    public void setShowExtensions(boolean showExtensions) {
        this.keyValueStorage.setBoolean(UserSettings.SHOW_EXTENSIONS, showExtensions);
    }

    public int getTheme() {
        return this.keyValueStorage.getInt(UserSettings.USER_THEME, DefaultSettings.THEME);
    }

    public void setTheme(int theme) {
        this.keyValueStorage.setInt(UserSettings.USER_THEME, theme);
    }

    public int getMaxZoom() {
        return this.keyValueStorage.getInt(UserSettings.MAX_ZOOM, DefaultSettings.MAX_ZOOM);
    }

    public void setMaxZoom(int maxZoom) {
        if (maxZoom < 1) {
            maxZoom = 1;
        }

        Settings.MAX_ZOOM = maxZoom;
        this.keyValueStorage.setInt(UserSettings.MAX_ZOOM, maxZoom);
    }

    public boolean getResetImageOnLink() {
        return this.keyValueStorage.getBoolean(UserSettings.RESET_IMAGE_ON_LINKING, DefaultSettings.RESET_IMAGE_ON_LINKING);
    }

    public void setResetImageOnLinking(boolean resetImageOnLinking) {
        Settings.RESET_IMAGE_ON_LINKING = resetImageOnLinking;

        this.keyValueStorage.setBoolean(UserSettings.RESET_IMAGE_ON_LINKING, resetImageOnLinking);
    }

    public int getMirroringType() {
        return this.keyValueStorage.getInt(UserSettings.MIRRORING_TYPE, DefaultSettings.MIRRORING_TYPE);
    }

    public void setMirroringType(int mirroringType) {
        Settings.MIRRORING_TYPE = mirroringType;

        this.keyValueStorage.setInt(UserSettings.MIRRORING_TYPE, mirroringType);
    }


    public void resetAllSettings() {
        this.keyValueStorage.remove(UserSettings.USER_THEME);
        this.keyValueStorage.remove(UserSettings.SYNCED_ZOOM);
        this.keyValueStorage.remove(UserSettings.SHOW_EXTENSIONS);
        this.keyValueStorage.remove(UserSettings.LAST_COMPARE_MODE);
        this.keyValueStorage.remove(UserSettings.MAX_ZOOM);
        Settings.MAX_ZOOM = DefaultSettings.MAX_ZOOM;
        this.keyValueStorage.remove(UserSettings.RESET_IMAGE_ON_LINKING);
        Settings.RESET_IMAGE_ON_LINKING = DefaultSettings.RESET_IMAGE_ON_LINKING;
        this.keyValueStorage.remove(UserSettings.MIRRORING_TYPE);
        Settings.MIRRORING_TYPE = DefaultSettings.MIRRORING_TYPE;

        this.LeftImageResizeSettings.resetAllSettings();
        this.RightImageResizeSettings.resetAllSettings();
    }
}

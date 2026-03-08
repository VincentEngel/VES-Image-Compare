package com.vincentengelsoftware.androidimagecompare.activities.settings.userSettings;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.services.settings.UserSettings;

/**
 * Pure-Java presenter for {@link UserSettingsActivity}.
 * <p>
 * Contains all business / settings logic with no Android UI dependencies,
 * making it straightforwardly testable with plain JUnit tests.
 * The Activity is responsible only for rendering the returned {@link UserSettingsUiState}
 * and calling the appropriate presenter methods in response to user actions.
 */
public class UserSettingsPresenter {

    static final float MIN_ZOOM_FALLBACK = 0.1F;
    static final int MAX_ZOOM_FALLBACK = 1;
    /** Total number of theme options: System, Light, Dark. */
    private static final int THEME_COUNT = 3;

    private final UserSettings userSettings;

    public UserSettingsPresenter(UserSettings userSettings) {
        this.userSettings = userSettings;
    }

    /**
     * Builds a complete {@link UserSettingsUiState} snapshot from the current
     * {@link UserSettings} values.
     */
    public UserSettingsUiState buildUiState() {
        int mirroringType = userSettings.getMirroringType();
        int tapHideMode   = userSettings.getTapHideMode();
        int theme         = userSettings.getTheme();

        return new UserSettingsUiState(
                String.valueOf(userSettings.getMaxZoom()),
                String.valueOf(userSettings.getMinZoom()),
                userSettings.getResetImageOnLink(),
                mirroringType == Status.NATURAL_MIRRORING,
                mirroringType == Status.STRICT_MIRRORING,
                mirroringType == Status.LOOSE_MIRRORING,
                mirroringExplanationResId(mirroringType),
                tapHideMode == Status.TAP_HIDE_MODE_INVISIBLE,
                tapHideMode == Status.TAP_HIDE_MODE_BACKGROUND,
                tapHideModeDescriptionResId(tapHideMode),
                themeButtonTextResId(theme)
        );
    }

    /**
     * Validates and saves the zoom values entered by the user.
     *
     */
    public SaveZoomResult saveZoom(int rawMaxZoom, float rawMinZoom) {
        boolean maxZoomClamped = false;
        boolean minZoomClamped = false;

        int maxZoom = rawMaxZoom;
        if (maxZoom < 1) {
            maxZoom = MAX_ZOOM_FALLBACK;
            maxZoomClamped = true;
        }

        float minZoom = rawMinZoom;
        if (minZoom <= 0F) {
            minZoom = MIN_ZOOM_FALLBACK;
            minZoomClamped = true;
        }

        userSettings.setMaxZoom(maxZoom);
        userSettings.setMinZoom(minZoom);

        return new SaveZoomResult(maxZoomClamped || minZoomClamped);
    }

    /** Cycles to the next theme (System → Light → Dark → System → …) and returns the new theme value. */
    public int cycleTheme() {
        int newTheme = (userSettings.getTheme() + 1) % THEME_COUNT;
        userSettings.setTheme(newTheme);
        return newTheme;
    }

    public void setResetImageOnLinking(boolean checked) {
        userSettings.setResetImageOnLinking(checked);
    }

    public void setMirroringType(int mirroringType) {
        userSettings.setMirroringType(mirroringType);
    }

    public void setTapHideMode(int tapHideMode) {
        userSettings.setTypHideMode(tapHideMode);
    }

    /** Resets all settings to their defaults and returns the resulting UI state. */
    public UserSettingsUiState resetAllSettings() {
        userSettings.resetAllSettings();
        return buildUiState();
    }

    public static int mirroringExplanationResId(int mirroringType) {
        return switch (mirroringType) {
            case Status.STRICT_MIRRORING -> R.string.settings_mirroring_strict_description;
            case Status.LOOSE_MIRRORING  -> R.string.settings_mirroring_loose_description;
            default                      -> R.string.settings_mirroring_natural_description;
        };
    }

    public static int tapHideModeDescriptionResId(int tapHideMode) {
        return tapHideMode == Status.TAP_HIDE_MODE_BACKGROUND
                ? R.string.settings_tap_hide_mode_description_background
                : R.string.settings_tap_hide_mode_description_invisible;
    }

    public static int themeButtonTextResId(int theme) {
        return switch (theme) {
            case Status.THEME_SYSTEM -> R.string.theme_system;
            case Status.THEME_LIGHT  -> R.string.theme_light;
            default                  -> R.string.theme_dark;
        };
    }

    public record SaveZoomResult(boolean hadInvalidInput) {}
}


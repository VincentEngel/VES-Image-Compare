package com.vincentengelsoftware.androidimagecompare.ui.settings.usersettings;

/**
 * Immutable snapshot of the UI state for {@link UserSettingsActivity}.
 *
 * @param maxZoom                        Maximum zoom level as a display string
 * @param minZoom                        Minimum zoom level as a display string
 * @param resetImageOnLinking            Whether reset-on-linking is enabled
 * @param mirroringNaturalChecked        Whether the Natural mirroring radio button is selected
 * @param mirroringStrictChecked         Whether the Strict mirroring radio button is selected
 * @param mirroringLooseChecked          Whether the Loose mirroring radio button is selected
 * @param mirroringExplanationResId      String resource ID for the current mirroring mode description
 * @param tapHideModeInvisibleChecked    Whether the Invisible tap-hide radio button is selected
 * @param tapHideModeBackgroundChecked   Whether the Background tap-hide radio button is selected
 * @param tapHideModeDescriptionResId    String resource ID for the current tap-hide mode description
 * @param themeButtonTextResId           String resource ID for the theme toggle button label
 */
public record UserSettingsUiState(
        String maxZoom,
        String minZoom,
        boolean resetImageOnLinking,
        boolean mirroringNaturalChecked,
        boolean mirroringStrictChecked,
        boolean mirroringLooseChecked,
        int mirroringExplanationResId,
        boolean tapHideModeInvisibleChecked,
        boolean tapHideModeBackgroundChecked,
        int tapHideModeDescriptionResId,
        int themeButtonTextResId
) {
}


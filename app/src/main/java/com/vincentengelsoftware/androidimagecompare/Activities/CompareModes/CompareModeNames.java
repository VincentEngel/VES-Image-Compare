package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

public class CompareModeNames {
    public static final String SIDE_BY_SIDE = "SIDE_BY_SIDE";
    public static final String OVERLAY_TAP = "OVERLAY_TAP";
    public static final String OVERLAY_SLIDE = "OVERLAY_SLIDE";
    public static final String OVERLAY_TRANSPARENT = "OVERLAY_TRANSPARENT";
    public static final String META_DATA = "META_DATA";

    public static String getInternalCompareModeNameFromUserCompareModeName(String compareMode)
    {
        return switch (compareMode) {
            case "Side by Side" -> SIDE_BY_SIDE;
            case "Overlay Tap" -> OVERLAY_TAP;
            case "Overlay Slide" -> OVERLAY_SLIDE;
            case "Transparent" -> OVERLAY_TRANSPARENT;
            case "MetaData" -> META_DATA;
            default -> "";
        };
    }
    public static String getInternalCompareModeNameFromActivityName(String activityName)
    {
        return switch (activityName) {
            case "SideBySideActivity" -> SIDE_BY_SIDE;
            case "OverlayTapActivity" -> OVERLAY_TAP;
            case "OverlaySlideActivity" -> OVERLAY_SLIDE;
            case "OverlayTransparentActivity" -> OVERLAY_TRANSPARENT;
            case "MetaDataActivity" -> META_DATA;
            default -> "";
        };
    }

    public static String getUserCompareModeNameFromInternalName(String compareMode)
    {
        return switch (compareMode) {
            case SIDE_BY_SIDE -> "Side by Side";
            case OVERLAY_TAP -> "Overlay Tap";
            case OVERLAY_SLIDE -> "Overlay Slide";
            case OVERLAY_TRANSPARENT -> "Transparent";
            case META_DATA -> "MetaData";
            default -> "";
        };

    }
}

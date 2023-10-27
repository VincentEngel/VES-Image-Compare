package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.content.Context;

import com.vincentengelsoftware.androidimagecompare.R;

public class CompareModeNames {
    public static final String SIDE_BY_SIDE = "SIDE_BY_SIDE";
    public static final String OVERLAY_TAP = "OVERLAY_TAP";
    public static final String OVERLAY_SLIDE = "OVERLAY_SLIDE";
    public static final String OVERLAY_TRANSPARENT = "OVERLAY_TRANSPARENT";
    public static final String META_DATA = "META_DATA";

    public static String getInternalCompareModeNameFromUserCompareModeName(Context context, String compareMode)
    {
        if (compareMode.equals(context.getString(R.string.compare_mode_side_by_side))) {
            return SIDE_BY_SIDE;
        }

        if (compareMode.equals(context.getString(R.string.compare_mode_overlay_tap))) {
            return OVERLAY_TAP;
        }

        if (compareMode.equals(context.getString(R.string.compare_mode_overlay_slide))) {
            return OVERLAY_SLIDE;
        }

        if (compareMode.equals(context.getString(R.string.compare_mode_transparent))) {
            return OVERLAY_TRANSPARENT;
        }

        if (compareMode.equals(context.getString(R.string.compare_mode_metadata))) {
            return META_DATA;
        }

        return "";
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

    public static String getUserCompareModeNameFromInternalName(Context context,String compareMode)
    {
        return switch (compareMode) {
            case SIDE_BY_SIDE -> context.getString(R.string.compare_mode_side_by_side);
            case OVERLAY_TAP -> context.getString(R.string.compare_mode_overlay_tap);
            case OVERLAY_SLIDE -> context.getString(R.string.compare_mode_overlay_slide);
            case OVERLAY_TRANSPARENT -> context.getString(R.string.compare_mode_transparent);
            case META_DATA -> context.getString(R.string.compare_mode_metadata);
            default -> "";
        };

    }
}

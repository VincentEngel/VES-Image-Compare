package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.content.Context;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.services.Settings.DefaultSettings;

public class CompareModeNames {
    public static final String SIDE_BY_SIDE = "SIDE_BY_SIDE";
    public static final String OVERLAY_TAP = "OVERLAY_TAP";
    public static final String OVERLAY_SLIDE = "OVERLAY_SLIDE";
    public static final String OVERLAY_TRANSPARENT = "OVERLAY_TRANSPARENT";
    public static final String META_DATA = "META_DATA";
    public static final String OVERLAY_CUT = "OVERLAY_CUT";

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

        if (compareMode.equals(context.getString(R.string.compare_mode_overlay_cut))) {
            return OVERLAY_CUT;
        }

        return DefaultSettings.COMPARE_MODE;
    }
    public static String getInternalCompareModeNameByActivity(Class<?> activity)
    {
        if (SideBySideActivity.class.equals(activity)) {
            return SIDE_BY_SIDE;
        }

        if (OverlayTapActivity.class.equals(activity)) {
            return OVERLAY_TAP;
        }

        if (OverlaySlideActivity.class.equals(activity)) {
            return OVERLAY_SLIDE;
        }

        if (OverlayTransparentActivity.class.equals(activity)) {
            return OVERLAY_TRANSPARENT;
        }

        if (MetaDataActivity.class.equals(activity)) {
            return META_DATA;
        }

        if (OverlayCutActivity.class.equals(activity)) {
            return OVERLAY_CUT;
        }

        return DefaultSettings.COMPARE_MODE;
    }

    public static String getUserCompareModeNameFromInternalName(Context context,String compareMode)
    {
        return switch (compareMode) {
            case SIDE_BY_SIDE -> context.getString(R.string.compare_mode_side_by_side);
            case OVERLAY_TAP -> context.getString(R.string.compare_mode_overlay_tap);
            case OVERLAY_SLIDE -> context.getString(R.string.compare_mode_overlay_slide);
            case OVERLAY_TRANSPARENT -> context.getString(R.string.compare_mode_transparent);
            case META_DATA -> context.getString(R.string.compare_mode_metadata);
            case OVERLAY_CUT -> context.getString(R.string.compare_mode_overlay_cut);
            default -> CompareModeNames.getUserCompareModeNameFromInternalName(context, DefaultSettings.COMPARE_MODE);
        };

    }
}

package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.vincentengelsoftware.androidimagecompare.R;

public class AppVersion {

    /** Returns the formatted version string (e.g. "v1.2.3"), or the fallback if unavailable. */
    public static String getVersionName(Context context) {
        try {
            PackageInfo pinfo = getPackageInfo(context);
            return "v" + pinfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return context.getString(R.string.unknown);
        }
    }

    @SuppressWarnings("deprecation")
    public static PackageInfo getPackageInfo(Context context) throws PackageManager.NameNotFoundException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA)
            );
        }
        return context.getPackageManager().getPackageInfo(
                context.getPackageName(),
                PackageManager.GET_META_DATA
        );
    }
}


package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.concurrent.TimeUnit;

public class ShouldAskForReview {
    public static final int INSTALLED_FOR_AT_LEAST_DAYS = 30;
    public static boolean check(Context context)
    {
        return !KeyValueStorage.getBoolean(context, KeyValueStorage.ASKED_FOR_REVIEW, false)
                && isAppInstalledForDays(context, ShouldAskForReview.INSTALLED_FOR_AT_LEAST_DAYS);
    }

    private static boolean isAppInstalledForDays(Context context, int days)
    {
        return getInstalledTimeInDays(context) >= days;
    }

    private static int getInstalledTimeInDays(Context context)
    {
        try {
            long firstInstallTime = context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                    .firstInstallTime;

            long currentTime = System.currentTimeMillis();

            long diff = currentTime - firstInstallTime;

            return (int) TimeUnit.MILLISECONDS.toDays(diff);
        } catch (Exception ignored) {}

        return 0;
    }
}

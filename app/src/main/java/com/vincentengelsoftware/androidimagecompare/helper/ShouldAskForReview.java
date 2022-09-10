package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;

import java.util.concurrent.TimeUnit;

public class ShouldAskForReview {
    public static boolean check(Context context)
    {
        return !KeyValueStorage.getBoolean(context, KeyValueStorage.ASKED_FOR_REVIEW, false)
                && isAppInstalledForDays(context, 30);
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
                    .getPackageInfo(context.getPackageName(), 0)
                    .firstInstallTime;

            long currentTime = System.currentTimeMillis();

            long diff = currentTime - firstInstallTime;

            return (int) TimeUnit.MILLISECONDS.toDays(diff);
        } catch (Exception ignored) {}

        return 0;
    }
}

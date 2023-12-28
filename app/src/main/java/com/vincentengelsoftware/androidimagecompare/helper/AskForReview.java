package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.pm.PackageManager;

import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;

import java.util.concurrent.TimeUnit;

public class AskForReview {
    public static final int INSTALLED_FOR_AT_LEAST_DAYS = 14;
    public static boolean isItTimeToAsk(Context context, KeyValueStorage keyValueStorage)
    {
        return !keyValueStorage.getBoolean(KeyValueStorage.ASKED_FOR_REVIEW, false)
                && isAppInstalledForDays(context, AskForReview.INSTALLED_FOR_AT_LEAST_DAYS);
    }

    private static boolean isAppInstalledForDays(Context context, int days)
    {
        return getInstalledTimeInDays(context) >= days;
    }

    private static int getInstalledTimeInDays(Context context)
    {
        try {
            long firstInstallTime = getFirstInstallTime(context);

            long currentTime = System.currentTimeMillis();

            long diff = currentTime - firstInstallTime;

            return (int) TimeUnit.MILLISECONDS.toDays(diff);
        } catch (Exception ignored) {}

        return 0;
    }

    @SuppressWarnings("deprecation")
    private static long getFirstInstallTime(Context context) throws PackageManager.NameNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return context
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA))
                    .firstInstallTime;
        }

        return context
                .getPackageManager()
                .getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA)
                .firstInstallTime;
    }
}

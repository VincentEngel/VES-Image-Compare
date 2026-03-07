package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;

import java.util.concurrent.TimeUnit;

public class AskForReview {
    private static final int INSTALLED_FOR_AT_LEAST_DAYS = 14;

    private static boolean isItTimeToAsk(Context context, KeyValueStorage keyValueStorage)
    {
        return !keyValueStorage.getBoolean(KeyValueStorage.ASKED_FOR_REVIEW, false)
                && isAppInstalledForDays(context, AskForReview.INSTALLED_FOR_AT_LEAST_DAYS);
    }

    public static void askForReviewWhenNecessary(Context context, KeyValueStorage keyValueStorage)
    {
        if (isItTimeToAsk(context, keyValueStorage)) {
            askForReview(context);
        }
    }

    private static void askForReview(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.alertDialog);

        builder.setCancelable(false);

        builder.setMessage(R.string.ask_for_review_text);

        builder.setPositiveButton(R.string.ask_for_review_positive, (dialogInterface, i) -> {
            if (IsPlaystoreInstalled.isPlayStoreInstalled(context)) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
            } else {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
            }
        });

        builder.setNegativeButton(R.string.ask_for_review_negative, (dialogInterface, i) -> {});

        builder.show();
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

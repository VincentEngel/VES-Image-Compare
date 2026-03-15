package com.vincentengelsoftware.androidimagecompare.ui.util;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AlertDialog;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.data.preferences.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.util.AppVersion;
import java.util.concurrent.TimeUnit;

public class AskForReview {
  private static final int INSTALLED_FOR_AT_LEAST_DAYS = 14;

  private static boolean isItTimeToAsk(Context context, KeyValueStorage keyValueStorage) {
    return !keyValueStorage.getBoolean(KeyValueStorage.ASKED_FOR_REVIEW, false)
        && isAppInstalledForDays(context, AskForReview.INSTALLED_FOR_AT_LEAST_DAYS);
  }

  public static void askForReviewWhenNecessary(Context context, KeyValueStorage keyValueStorage) {
    if (isItTimeToAsk(context, keyValueStorage)) {
      askForReview(context);
    }
  }

  private static void askForReview(Context context) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.alertDialog);

    builder.setCancelable(false);

    builder.setMessage(R.string.ask_for_review_text);

    builder.setPositiveButton(
        R.string.ask_for_review_positive,
        (dialogInterface, i) -> PlayStoreNavigator.openPlayStoreAppPage(context));

    builder.setNegativeButton(R.string.ask_for_review_negative, (dialogInterface, i) -> {});

    builder.show();
  }

  private static boolean isAppInstalledForDays(Context context, int days) {
    return getInstalledTimeInDays(context) >= days;
  }

  private static int getInstalledTimeInDays(Context context) {
    try {
      long firstInstallTime = getFirstInstallTime(context);
      long currentTime = System.currentTimeMillis();
      long diff = currentTime - firstInstallTime;
      return (int) TimeUnit.MILLISECONDS.toDays(diff);
    } catch (Exception ignored) {
    }

    return 0;
  }

  private static long getFirstInstallTime(Context context)
      throws PackageManager.NameNotFoundException {
    return AppVersion.getPackageInfo(context).firstInstallTime;
  }
}

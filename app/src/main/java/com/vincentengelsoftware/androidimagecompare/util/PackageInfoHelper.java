package com.vincentengelsoftware.androidimagecompare.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class PackageInfoHelper {
  @SuppressWarnings("deprecation")
  public static PackageInfo getPackageInfo(Context context)
      throws PackageManager.NameNotFoundException {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      return context
          .getPackageManager()
          .getPackageInfo(
              context.getPackageName(),
              PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
    }

    return context
        .getPackageManager()
        .getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
  }

  public static String getPackageVersion(Context context)
      throws PackageManager.NameNotFoundException {
    PackageInfo pinfo = getPackageInfo(context);
    return pinfo.versionName;
  }
}

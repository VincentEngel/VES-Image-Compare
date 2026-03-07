package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class IsPlaystoreInstalled {
    public static boolean isPlayStoreInstalled(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.getPackageManager().getPackageInfo("com.android.vending", PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
            } else {
                context.getPackageManager().getPackageInfo("com.android.vending", PackageManager.GET_META_DATA);
            }

            return true;
        } catch (Exception ignored) {
        }

        return false;
    }
}

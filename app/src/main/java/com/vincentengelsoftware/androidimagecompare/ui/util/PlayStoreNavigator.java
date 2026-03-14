package com.vincentengelsoftware.androidimagecompare.ui.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PlayStoreNavigator {

    /** Opens the Play Store page for this app, falling back to the browser. */
    public static void openPlayStoreAppPage(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (ActivityNotFoundException e) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
            } catch (ActivityNotFoundException ignored) {
            }
        }
    }
}


package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class PlayStoreNavigator {

    /** Opens the Play Store listing for this app, falling back to the browser. */
    public static void openPlayStoreListing(Context context) {
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


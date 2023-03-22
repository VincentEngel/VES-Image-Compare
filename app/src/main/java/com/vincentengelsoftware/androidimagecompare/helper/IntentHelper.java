package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import java.util.ArrayList;

public class IntentHelper {
    @SuppressWarnings("deprecation")
    public static Uri getOutOfParcelableExtra(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri.class);
        }

        return intent.getParcelableExtra(Intent.EXTRA_STREAM);
    }

    @SuppressWarnings("deprecation")
    public static ArrayList<Uri> getOutOfParcelableArrayListExtra(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri.class);
        }

        return intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    }
}

package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.Context;

import java.io.File;

public class CacheClearer {
    public static void clear(Context context)
    {
        try {
            for (File file : context.getCacheDir().listFiles()) {
                file.delete();
            }
        } catch (Exception ignored) {
        }
    }
}

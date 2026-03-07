package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.InputStream;

public class BitmapExtractor {
    public static Bitmap fromUri(ContentResolver cr, Uri uri)
    {
        Bitmap bitmap;

        try {
            InputStream input = cr.openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(input);
            input.close();
        } catch (Exception ignored) {
            // TODO create and return error BitmapImage
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        }

        return bitmap;
    }

    public static Bitmap fromUriString(ContentResolver cr, String uriString)
    {
        try {
            Uri uri = Uri.parse(uriString);
            if ("file".equals(uri.getScheme())) {
                return BitmapFactory.decodeFile(uri.getPath());
            }
            return fromUri(cr, uri);
        } catch (Exception ignored) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        }
    }
}

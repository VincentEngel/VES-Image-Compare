package com.vincentengelsoftware.androidimagecompare.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;

import java.io.File;
import java.io.InputStream;

public class ImageHolder {
    public Uri uri = null;
    public File file;
    public String filePath;
    public Bitmap bitmap;
    public Bitmap bitmapSmall;
    public Bitmap bitmapScreenSize;

    private final int MAX_SMALL_SIZE = 328;

    public void updateFromImageHolder(ImageHolder imageHolder)
    {
        this.uri = imageHolder.uri;
        this.file = imageHolder.file;
        this.filePath = imageHolder.filePath;
        this.bitmap = imageHolder.bitmap;
        this.bitmapSmall = imageHolder.bitmapSmall;
        this.bitmapScreenSize = imageHolder.bitmapScreenSize;
    }

    public void updateFromUri(Uri uri, ContentResolver cr, Point point)
    {
        this.uri = uri;

        try {
            InputStream input = cr.openInputStream(uri);
            this.bitmap = ExifUtil.rotateBitmap(BitmapFactory.decodeStream(input), new ExifInterface(input));
            input.close();

            this.bitmapSmall = BitmapHelper.resizeBitmap(
                    this.bitmap,
                    MAX_SMALL_SIZE,
                    MAX_SMALL_SIZE
            );

            this.bitmapScreenSize = BitmapHelper.resizeBitmap(
                    this.bitmap,
                    Math.max(point.x, point.y),
                    Math.max(point.x, point.y)
            );
        } catch (Exception ignored) {
        }

    }
}

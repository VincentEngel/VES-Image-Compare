package com.vincentengelsoftware.androidimagecompare.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.vincentengelsoftware.androidimagecompare.MainActivity;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageHolder {
    String name;
    public Uri uri = null;
    public Uri uriScreenSize;
    public Bitmap bitmap;
    private Bitmap bitmapSmall;
    private Bitmap bitmapScreenSize;

    private Point point;
    private ContentResolver contentResolver;
    private DisplayMetrics displayMetrics;

    private final float MAX_SMALL_SIZE_DP = 164.499f;

    public ImageHolder(String name)
    {
        this.name = name;
    }

    public void updateFromImageHolder(ImageHolder imageHolder)
    {
        this.uri = imageHolder.uri;
        this.point = imageHolder.point;
        this.contentResolver = imageHolder.contentResolver;
        this.displayMetrics = imageHolder.displayMetrics;
        this.bitmapSmall = imageHolder.bitmapSmall;
        this.bitmapScreenSize = imageHolder.bitmapScreenSize;
        this.uriScreenSize = imageHolder.uriScreenSize;
    }

    public Bitmap getBitmapSmall()
    {
        if (this.bitmapSmall ==  null) {
            int maxSideLength = Math.round(
                    TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            MAX_SMALL_SIZE_DP,
                            displayMetrics
                    )
            );

            this.bitmapSmall = BitmapHelper.resizeBitmap(
                    this.bitmap,
                    maxSideLength,
                    maxSideLength
            );
        }

        return this.bitmapSmall;
    }

    public Bitmap getBitmapScreenSize()
    {
        if (this.bitmapScreenSize == null) {
            this.bitmapScreenSize = BitmapHelper.resizeBitmap(
                    this.bitmap,
                    Math.max(point.x, point.y),
                    Math.max(point.x, point.y)
            );
        }

        return this.bitmapScreenSize;
    }

    public Uri getUriScreenSize()
    {
        if (this.uriScreenSize != null) {
            return this.uriScreenSize;
        }

        return this.uri;
    }

    public void updateFromUri(Uri uri, ContentResolver cr, Point point, DisplayMetrics displayMetrics, Context context)
    {
        this.uri = uri;
        this.point = point;
        this.contentResolver = cr;
        this.displayMetrics = displayMetrics;
        this.bitmapSmall = null;
        this.bitmapScreenSize = null;
        this.uriScreenSize = null;

        try {
            InputStream input = cr.openInputStream(uri);
            this.bitmap = ExifUtil.rotateBitmap(BitmapFactory.decodeStream(input), new ExifInterface(input));
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ImageHolder that = this;

        new Thread(() -> {
            File temp;

            try {
                temp = File.createTempFile(that.name, null, context.getCacheDir());
                FileOutputStream out = new FileOutputStream(temp);
                that.getBitmapScreenSize().compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
                uriScreenSize = Uri.parse(temp.getPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

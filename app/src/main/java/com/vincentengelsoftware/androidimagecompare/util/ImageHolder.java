package com.vincentengelsoftware.androidimagecompare.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;

import java.io.InputStream;

public class ImageHolder {
    String name;
    public Uri uri = null;
    public Bitmap bitmap;
    private Bitmap bitmapSmall;
    private Bitmap bitmapScreenSize;

    private Point point;
    private ContentResolver contentResolver;
    private DisplayMetrics displayMetrics;

    private int currentRotation = 0;
    private int currentBitmapRotation = 0;

    private final float MAX_SMALL_SIZE_DP = 164.499f;

    public ImageHolder(String name)
    {
        this.name = name;
    }

    public int getRotationDegree()
    {
        if (this.currentRotation == 3) {
            this.currentRotation = 0;
        } else {
            this.currentRotation++;
        }

        return 90;
    }

    public void updateFromImageHolder(ImageHolder imageHolder)
    {
        this.uri = imageHolder.uri;
        this.point = imageHolder.point;
        this.contentResolver = imageHolder.contentResolver;
        this.displayMetrics = imageHolder.displayMetrics;
        this.bitmap = imageHolder.bitmap;
        this.bitmapSmall = imageHolder.bitmapSmall;
        this.bitmapScreenSize = imageHolder.bitmapScreenSize;
        this.currentRotation = imageHolder.currentRotation;
        this.currentBitmapRotation = imageHolder.currentBitmapRotation;
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

    public void updateFromUri(Uri uri, ContentResolver cr, Point point, DisplayMetrics displayMetrics)
    {
        this.uri = uri;
        this.point = point;
        this.contentResolver = cr;
        this.displayMetrics = displayMetrics;
        this.bitmapSmall = null;
        this.bitmapScreenSize = null;

        try {
            InputStream input = cr.openInputStream(uri);
            this.bitmap = BitmapFactory.decodeStream(input);
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rotateImage()
    {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(this.getRotationDegree());

            this.bitmap = Bitmap.createBitmap(
                    this.bitmap,
                    0,
                    0,
                    this.bitmap.getWidth(),
                    this.bitmap.getHeight(),
                    matrix,
                    true
            );
            this.bitmapSmall = null;
            this.bitmapScreenSize = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

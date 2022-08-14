package com.vincentengelsoftware.androidimagecompare.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;

import java.io.InputStream;

public class ImageHolder {
    public Uri uri = null;
    public Bitmap bitmap;
    private Bitmap bitmapSmall;
    private Bitmap bitmapScreenSize;
    public Bitmap rotatedBitmap;

    private Point point;
    private DisplayMetrics displayMetrics;

    private int currentRotation = 0;
    private int currentBitmapRotation = 0;
    private static final int BASE_DEGREE = 90;

    private final float MAX_SMALL_SIZE_DP = 164.499f;

    private int getRotationDegree()
    {
        if (this.currentRotation == 3) {
            this.currentRotation = 0;
        } else {
            this.currentRotation++;
        }

        return BASE_DEGREE;
    }

    public void updateFromImageHolder(ImageHolder imageHolder)
    {
        this.uri = imageHolder.uri;
        this.point = imageHolder.point;
        this.displayMetrics = imageHolder.displayMetrics;

        this.bitmap = imageHolder.bitmap;
        this.bitmapSmall = imageHolder.bitmapSmall;
        this.bitmapScreenSize = imageHolder.bitmapScreenSize;
        this.rotatedBitmap = imageHolder.rotatedBitmap;

        this.currentRotation = imageHolder.currentRotation;
        this.currentBitmapRotation = imageHolder.currentBitmapRotation;
    }

    public void calculateRotatedBitmap()
    {
        if (this.currentBitmapRotation == this.currentRotation && this.rotatedBitmap == null) {
            this.rotatedBitmap = this.bitmap;
            return;
        }

        if (this.rotatedBitmap == null || this.currentBitmapRotation != this.currentRotation) {
            this.rotatedBitmap = BitmapHelper.rotateBitmap(this.bitmap, BASE_DEGREE * this.currentRotation);
            this.bitmapScreenSize = null;
            this.getBitmapScreenSize();
            this.currentBitmapRotation = this.currentRotation;
        }
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
                    this.rotatedBitmap,
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
        this.displayMetrics = displayMetrics;
        this.bitmapSmall = null;
        this.bitmapScreenSize = null;
        this.rotatedBitmap = null;

        this.currentRotation = 0;
        this.currentBitmapRotation = 0;

        try {
            InputStream input = cr.openInputStream(uri);
            this.bitmap = BitmapFactory.decodeStream(input);
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rotatePreviewImage()
    {
        this.bitmapSmall = BitmapHelper.rotateBitmap(
                this.getBitmapSmall(),
                this.getRotationDegree()
        );
    }
}

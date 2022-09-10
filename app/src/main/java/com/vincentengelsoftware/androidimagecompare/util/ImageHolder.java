package com.vincentengelsoftware.androidimagecompare.util;

import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageView;

import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class ImageHolder {
    private Bitmap bitmap;
    private Bitmap bitmapSmall;
    private Bitmap bitmapScreenSize;
    private Bitmap rotatedBitmap;

    private int currentRotation = 0;
    private int currentBitmapRotation = 0;
    private static final int BASE_DEGREE = 90;

    public static final float MAX_SMALL_SIZE_DP = 164.499f;

    private int maxSideSize;
    private int maxSideSizeForSmallBitmap;

    private String imageName;

    private boolean resizeImageToScreen = false;

    private Uri cameraUri = null;

    public Uri getCameraUri() {
        return cameraUri;
    }

    public void setCameraUri(Uri cameraUri) {
        this.cameraUri = cameraUri;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    private int getRotationDegree()
    {
        if (this.currentRotation == 3) {
            this.currentRotation = 0;
        } else {
            this.currentRotation++;
        }

        return BASE_DEGREE;
    }

    public String getImageName()
    {
        return this.imageName;
    }

    public void updateFromImageHolder(ImageHolder imageHolder)
    {
        this.maxSideSize = imageHolder.maxSideSize;
        this.maxSideSizeForSmallBitmap = imageHolder.maxSideSizeForSmallBitmap;

        this.bitmap = imageHolder.bitmap;
        this.bitmapSmall = imageHolder.bitmapSmall;
        this.bitmapScreenSize = imageHolder.bitmapScreenSize;
        this.rotatedBitmap = imageHolder.rotatedBitmap;

        this.currentRotation = imageHolder.currentRotation;
        this.currentBitmapRotation = imageHolder.currentBitmapRotation;

        this.imageName = imageHolder.imageName;

        this.resizeImageToScreen = imageHolder.resizeImageToScreen;
    }

    public void calculateRotatedBitmap()
    {
        if (this.currentBitmapRotation == this.currentRotation && this.rotatedBitmap == null) {
            this.rotatedBitmap = this.bitmap;
            return;
        }

        if (this.rotatedBitmap == null || this.currentBitmapRotation != this.currentRotation) {
            if (this.currentRotation == 0) {
                this.rotatedBitmap = this.bitmap;
            } else {
                this.rotatedBitmap = BitmapHelper.rotateBitmap(this.bitmap, BASE_DEGREE * this.currentRotation);
            }

            this.bitmapScreenSize = null;
            this.getBitmapScreenSize();
            this.currentBitmapRotation = this.currentRotation;
        }
    }

    public Bitmap getBitmapSmall()
    {
        if (this.bitmapSmall ==  null) {
            this.bitmapSmall = BitmapHelper.resizeBitmap(
                    this.bitmap,
                    this.maxSideSizeForSmallBitmap,
                    this.maxSideSizeForSmallBitmap
            );
        }

        return this.bitmapSmall;
    }

    public Bitmap getBitmapScreenSize()
    {
        if (this.bitmapScreenSize == null) {
            this.bitmapScreenSize = BitmapHelper.resizeBitmap(
                    this.rotatedBitmap,
                    this.maxSideSize,
                    this.maxSideSize
            );
        }

        return this.bitmapScreenSize;
    }

    public void updateFromBitmap(
            Bitmap bitmap,
            int maxSideSize,
            int maxSideSizeForSmallBitmap,
            String imageName
    )
    {
        this.resetProperties();

        this.bitmap = bitmap;
        this.imageName = imageName;
        this.maxSideSize = maxSideSize;
        this.maxSideSizeForSmallBitmap = maxSideSizeForSmallBitmap;
    }

    private void resetProperties()
    {
        this.bitmapSmall = null;
        this.bitmapScreenSize = null;
        this.rotatedBitmap = null;

        this.currentRotation = 0;
        this.currentBitmapRotation = 0;
    }

    public void rotatePreviewImage()
    {
        this.bitmapSmall = BitmapHelper.rotateBitmap(
                this.getBitmapSmall(),
                this.getRotationDegree()
        );
    }

    public boolean isResizeImageToScreen() {
        return this.resizeImageToScreen;
    }

    public void setResizeImageToScreen(boolean resizeImageToScreen) {
        this.resizeImageToScreen = resizeImageToScreen;
    }

    public Bitmap getAdjustedBitmap()
    {
        if (this.resizeImageToScreen) {
            return this.getBitmapScreenSize();
        }

        return this.rotatedBitmap;
    }

    public void updateVesImageViewWithAdjustedImage(VesImageInterface imageView)
    {
        imageView.setBitmapImage(this.getAdjustedBitmap());
    }

    public void updateImageViewPreviewImage(ImageView imageView)
    {
        imageView.setImageBitmap(this.getBitmapSmall());
    }
}

package com.vincentengelsoftware.androidimagecompare.util;

import android.graphics.Bitmap;
import android.widget.ImageView;
import com.vincentengelsoftware.androidimagecompare.globals.ImageResizeOptions;
import com.vincentengelsoftware.androidimagecompare.helper.BitmapHelper;

public class ImageInfoHolder {
    private Bitmap bitmap;
    private Bitmap bitmapSmall;
    private Bitmap bitmapResized;
    private Bitmap rotatedBitmap;

    private int currentRotation = 0;
    private int currentBitmapRotation = 0;

    private String imageName;

    private int resizeOption = ImageResizeOptions.RESIZE_OPTION_AUTOMATIC;

    // Should be part of the constructor as they never change
    private int maxSideSize;
    private int maxSideSizeForSmallBitmap;

    private int customHeight;
    private int customWidth;

    private static final int BASE_DEGREE = 90;

    // ---- snapshot of the state at the time the compare file was last written ----
    /** The bitmap instance that was active when the compare file was last saved. */
    private Bitmap savedBitmapRef = null;
    /** The rotation that was active when the compare file was last saved. */
    private int savedRotation = -1;
    /** The resize option that was active when the compare file was last saved. */
    private int savedResizeOption = -1;
    /** Custom height that was active when the compare file was last saved. */
    private int savedCustomHeight = 0;
    /** Custom width that was active when the compare file was last saved. */
    private int savedCustomWidth = 0;

    /**
     * Returns {@code true} when the compare-output file must be regenerated because the
     * image, its rotation, or its resize settings have changed since the last save, or
     * because the file does not exist yet.
     *
     * @param compareFile the cache file that would be (re-)written
     */
    public boolean needsResave(java.io.File compareFile) {
        if (!compareFile.exists()) return true;
        if (savedBitmapRef != bitmap) return true;
        if (savedRotation != currentRotation) return true;
        if (savedResizeOption != resizeOption) return true;
        if (resizeOption == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
            if (savedCustomHeight != customHeight) return true;
            if (savedCustomWidth != customWidth) return true;
        }
        return false;
    }

    /**
     * Records the current image state so that subsequent calls to {@link #needsResave}
     * can detect whether anything has changed.
     */
    public void markSaved() {
        savedBitmapRef = bitmap;
        savedRotation = currentRotation;
        savedResizeOption = resizeOption;
        savedCustomHeight = customHeight;
        savedCustomWidth = customWidth;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public String getImageName()
    {
        return this.imageName;
    }

    public void updateFromImageHolder(ImageInfoHolder imageInfoHolder)
    {
        this.maxSideSize = imageInfoHolder.maxSideSize;
        this.maxSideSizeForSmallBitmap = imageInfoHolder.maxSideSizeForSmallBitmap;

        this.bitmap = imageInfoHolder.bitmap;
        this.bitmapSmall = imageInfoHolder.bitmapSmall;
        this.bitmapResized = null;
        this.rotatedBitmap = imageInfoHolder.rotatedBitmap;

        this.currentRotation = imageInfoHolder.currentRotation;
        this.currentBitmapRotation = imageInfoHolder.currentBitmapRotation;

        this.imageName = imageInfoHolder.imageName;
    }

    /**
     * Computes the final bitmap for comparison by scaling first, then rotating.
     * Resizing before rotation is significantly cheaper: the rotation matrix operates on
     * a much smaller bitmap, reducing both CPU time and peak heap usage.
     */
    public void calculateRotatedBitmap()
    {
        // Already calculated
        if (
                this.currentBitmapRotation == this.currentRotation
                        && this.rotatedBitmap != null
                        && this.bitmapResized != null
        ) {
            return;
        }

        // Step 1: scale the full-resolution source bitmap to the target size first.
        Bitmap scaledBitmap;
        if (this.resizeOption == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
            scaledBitmap = BitmapHelper.resizeBitmap(this.bitmap, this.customWidth, this.customHeight);
        } else if (this.resizeOption == ImageResizeOptions.RESIZE_OPTION_AUTOMATIC) {
            scaledBitmap = BitmapHelper.createScaledBitmapToMaxLength(
                    this.bitmap, this.maxSideSize, this.maxSideSize);
        } else {
            // RESIZE_OPTION_ORIGINAL — keep at full resolution
            scaledBitmap = this.bitmap;
        }

        // Step 2: rotate the already-small bitmap.
        if (this.currentRotation == 0) {
            this.rotatedBitmap = scaledBitmap;
        } else {
            this.rotatedBitmap = BitmapHelper.rotateBitmap(scaledBitmap, BASE_DEGREE * this.currentRotation);
        }

        // bitmapResized now equals rotatedBitmap (scaling was already done above).
        this.bitmapResized = this.rotatedBitmap;
        this.currentBitmapRotation = this.currentRotation;

    }

    public Bitmap getBitmapSmall()
    {
        if (this.bitmapSmall ==  null) {
            this.bitmapSmall = BitmapHelper.createScaledBitmapToMaxLength(
                    this.bitmap,
                    this.maxSideSizeForSmallBitmap,
                    this.maxSideSizeForSmallBitmap
            );
        }

        return this.bitmapSmall;
    }

    public Bitmap getBitmapResized()
    {
        if (this.bitmapResized == null) {
            if (this.resizeOption == ImageResizeOptions.RESIZE_OPTION_CUSTOM) {
                this.bitmapResized = BitmapHelper.resizeBitmap(
                        this.rotatedBitmap,
                        this.customWidth,
                        this.customHeight
                );
            } else {
                this.bitmapResized = BitmapHelper.createScaledBitmapToMaxLength(
                        this.rotatedBitmap,
                        this.maxSideSize,
                        this.maxSideSize
                );
            }
        }

        return this.bitmapResized;
    }

    public void resetBitmapResized()
    {
        this.bitmapResized = null;
    }

    public void updateFromBitmap(
            Bitmap bitmap,
            int maxSideSize,
            int maxSideSizeForSmallBitmap,
            String imageName
    ) {
        this.resetProperties();

        this.bitmap = bitmap;
        this.imageName = imageName;
        this.maxSideSize = maxSideSize;
        this.maxSideSizeForSmallBitmap = maxSideSizeForSmallBitmap;
    }

    public void setResizeOption(int resizeOption)
    {
        this.resizeOption = resizeOption;
        // Invalidate snapshot so needsResave() picks up the change.
        this.savedResizeOption = -1;
    }

    public void setCustomSize(int height, int width)
    {
        this.customHeight = height;
        this.customWidth = width;
        // Invalidate snapshot so needsResave() picks up the change.
        this.savedCustomHeight = 0;
        this.savedCustomWidth = 0;
    }

    private void resetProperties()
    {
        this.bitmapSmall = null;
        this.bitmapResized = null;
        this.rotatedBitmap = null;

        this.currentRotation = 0;
        this.currentBitmapRotation = 0;

        // Invalidate the saved-state snapshot so needsResave() correctly returns true
        // for this freshly loaded image, regardless of what was previously saved.
        this.savedBitmapRef = null;
        this.savedRotation = -1;
        this.savedResizeOption = -1;
        this.savedCustomHeight = 0;
        this.savedCustomWidth = 0;
    }

    public void rotatePreviewImage()
    {
        if (this.currentRotation == 3) {
            this.currentRotation = 0;
        } else {
            this.currentRotation++;
        }

        this.bitmapSmall = BitmapHelper.rotateBitmap(
                this.getBitmapSmall(),
                BASE_DEGREE
        );
    }


    public Bitmap getAdjustedBitmap()
    {
        if (
                this.resizeOption == ImageResizeOptions.RESIZE_OPTION_AUTOMATIC
                || this.resizeOption == ImageResizeOptions.RESIZE_OPTION_CUSTOM
        ) {
            return this.getBitmapResized();
        }

        return this.rotatedBitmap;
    }

    public void updateImageViewPreviewImage(ImageView imageView)
    {
        imageView.setImageBitmap(this.getBitmapSmall());
    }
}

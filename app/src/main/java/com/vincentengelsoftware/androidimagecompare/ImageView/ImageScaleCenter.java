package com.vincentengelsoftware.androidimagecompare.ImageView;

public class ImageScaleCenter {
    private final float scale;
    private final float centerX;
    private final float centerY;

    public ImageScaleCenter(float scale, float centerX, float centerY) {
        this.scale = scale;
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public float getScale() {
        return scale;
    }

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }
}

package com.vincentengelsoftware.androidimagecompare.services.Settings;

import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;

public class ImageResizeSettings {
    private int imageResizeOption;
    private int imageResizeWidth;
    private int imageResizeHeight;

    public static final String IMAGE_RESIZE_OPTION = "IMAGE_RESIZE_OPTION";
    public static final String IMAGE_RESIZE_WIDTH = "IMAGE_RESIZE_WIDTH";
    public static final String IMAGE_RESIZE_HEIGHT = "IMAGE_RESIZE_HEIGHT";

    private final String PREFIX;
    private final KeyValueStorage keyValueStorage;

    public ImageResizeSettings(String prefix, KeyValueStorage keyValueStorage) {
        this.PREFIX = prefix;
        this.keyValueStorage = keyValueStorage;

        this.imageResizeOption = this.keyValueStorage.getInt(this.PREFIX + ImageResizeSettings.IMAGE_RESIZE_OPTION, DefaultSettings.IMAGE_RESIZE_OPTION);
        this.imageResizeWidth = this.keyValueStorage.getInt(this.PREFIX + ImageResizeSettings.IMAGE_RESIZE_WIDTH, DefaultSettings.IMAGE_RESIZE_WIDTH);
        this.imageResizeHeight = this.keyValueStorage.getInt(this.PREFIX + ImageResizeSettings.IMAGE_RESIZE_HEIGHT, DefaultSettings.IMAGE_RESIZE_HEIGHT);
    }

    public int getImageResizeOption() {
        return imageResizeOption;
    }

    public void setImageResizeOption(int imageResizeOption) {
        this.imageResizeOption = imageResizeOption;
        this.keyValueStorage.setInt(this.PREFIX + ImageResizeSettings.IMAGE_RESIZE_OPTION, imageResizeOption);
    }

    public int getImageResizeWidth() {
        return imageResizeWidth;
    }

    public void setImageResizeWidth(int imageResizeWidth) {
        this.imageResizeWidth = imageResizeWidth;
        this.keyValueStorage.setInt(this.PREFIX + ImageResizeSettings.IMAGE_RESIZE_WIDTH, imageResizeWidth);
    }

    public int getImageResizeHeight() {
        return imageResizeHeight;
    }

    public void setImageResizeHeight(int imageResizeHeight) {
        this.imageResizeHeight = imageResizeHeight;
        this.keyValueStorage.setInt(this.PREFIX + ImageResizeSettings.IMAGE_RESIZE_HEIGHT, imageResizeHeight);
    }
}

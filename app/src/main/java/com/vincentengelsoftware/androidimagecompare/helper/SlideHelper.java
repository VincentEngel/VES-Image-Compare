package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

public class SlideHelper {
    public static Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float)maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float)maxWidth / ratioBitmap);
        }
        image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
        return image;
    }

    public static void setSwapSlideDirectionOnClick(
            ImageButton imageButton,
            SeekBar seekBar,
            UtilMutableBoolean mutableBoolean
    ) {
        imageButton.setOnClickListener(view -> {
            mutableBoolean.value = !mutableBoolean.value;
            int progress = 50;
            // onProgressChanged is not triggered if setProgress is called with current progress
            if (seekBar.getProgress() == progress) {
                progress = 51;
            }
            seekBar.setProgress(progress);
        });
    }

    public static void addSeekbarLogic(
            SeekBar seekBar,
            VesImageInterface imageView,
            UtilMutableBoolean mutableBoolean,
            Bitmap bitmapSource
    ) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int width = bitmapSource.getWidth() * i / 100;

                if (width == 0) {
                    width = 1;
                }

                Bitmap bitmapCopy = bitmapSource.copy(bitmapSource.getConfig(), true);

                int[] pixels = new int[bitmapSource.getHeight()*bitmapSource.getWidth()];
                bitmapSource.getPixels(
                        pixels,
                        0,
                        bitmapSource.getWidth(),
                        0,
                        0,
                        bitmapSource.getWidth(),
                        bitmapSource.getHeight()
                );

                if (mutableBoolean.value) {
                    for (int x = width; x < bitmapSource.getWidth(); x++) {
                        for (int y = 0; y < bitmapSource.getHeight(); y++) {
                            pixels[x + (y * bitmapSource.getWidth())] = Color.TRANSPARENT;
                        }
                    }
                } else {
                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < bitmapSource.getHeight(); y++) {
                            pixels[x + (y * bitmapSource.getWidth())] = Color.TRANSPARENT;
                        }
                    }
                }

                bitmapCopy.setPixels(
                        pixels,
                        0,
                        bitmapSource.getWidth(),
                        0,
                        0,
                        bitmapSource.getWidth(),
                        bitmapSource.getHeight()
                );

                imageView.setBitmapImage(bitmapCopy);

                if (
                        (!mutableBoolean.value && seekBar.getProgress() >= 98)
                        || (mutableBoolean.value && seekBar.getProgress() <= 2)
                ) {
                    imageView.setVisibility(View.GONE);
                } else {
                    imageView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }
}

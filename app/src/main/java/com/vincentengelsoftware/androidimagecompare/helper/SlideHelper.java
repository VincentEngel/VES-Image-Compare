package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

import com.vincentengelsoftware.androidimagecompare.R;

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

            if (mutableBoolean.value) {
                imageButton.setImageResource(R.drawable.ic_slide_ltr_vector);
            } else {
                imageButton.setImageResource(R.drawable.ic_slide_rtl_vector);
            }

            int progress = 50;
            // onProgressChanged is not triggered if setProgress is called with current progress
            if (seekBar.getProgress() == progress) {
                progress = 51;
            }
            seekBar.setProgress(progress);
        });
    }

    /**
     * TODO: Improve speed(?) and move it to a thread
     */
    public static void addSeekbarLogic(
            SeekBar seekBar,
            VesImageInterface imageView,
            UtilMutableBoolean cutFromRightToLeft,
            Bitmap bitmapSource,
            ImageButton hideShow
    ) {
        Bitmap transparentBitmap = BitmapHelper.createTransparentBitmap(
                bitmapSource.getWidth(),
                bitmapSource.getHeight()
        );

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!cutFromRightToLeft.value && (seekBar.getProgress() >= 99)) {
                    imageView.setVisibility(View.GONE);
                    return;
                }

                int width = bitmapSource.getWidth() * i / 100;

                if (
                        cutFromRightToLeft.value
                                && ((seekBar.getProgress() <= 1) || width == 0)
                ) {
                    imageView.setVisibility(View.GONE);
                    return;
                }

                imageView.setBitmapImage(
                        BitmapHelper.getCutBitmapWithTransparentBackgroundWithCanvas(
                                bitmapSource,
                                transparentBitmap,
                                width,
                                cutFromRightToLeft.value
                        )
                );

                hideShow.setImageResource(R.drawable.ic_unhide_vector);
                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}

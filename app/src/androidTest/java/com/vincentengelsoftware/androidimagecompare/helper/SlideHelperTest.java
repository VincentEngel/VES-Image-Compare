package com.vincentengelsoftware.androidimagecompare.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class SlideHelperTest {
    @Test
    public void setSwapSlideDirectionOnClick()
    {
        ImageButton imageButton = new ImageButton(ApplicationProvider.getApplicationContext());
        SeekBar seekBar = new SeekBar(ApplicationProvider.getApplicationContext());
        UtilMutableBoolean mutableBoolean = new UtilMutableBoolean();
        SlideHelper.setSwapSlideDirectionOnClick(
                imageButton,
                seekBar,
                mutableBoolean
        );

        seekBar.setProgress(0);
        imageButton.callOnClick();
        assertEquals(50, seekBar.getProgress());
        assertFalse(mutableBoolean.value);

        seekBar.setProgress(100);
        imageButton.callOnClick();
        assertEquals(50, seekBar.getProgress());
        assertTrue(mutableBoolean.value);

        seekBar.setProgress(50);
        imageButton.callOnClick();
        assertEquals(51, seekBar.getProgress());
        assertFalse(mutableBoolean.value);

        seekBar.setProgress(51);
        imageButton.callOnClick();
        assertEquals(50, seekBar.getProgress());
        assertTrue(mutableBoolean.value);
    }

    @Test
    public void addSeekbarLogic()
    {
        /**
         * SeekBar seekBar = new SeekBar(ApplicationProvider.getApplicationContext());
         *         UtilMutableBoolean mutableBoolean = new UtilMutableBoolean();
         *         ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
         *         int width = 100, height = 100;
         *
         *         int[] pixels = new int[width*height];
         *         Arrays.fill(pixels, Color.BLACK);
         *         Bitmap bitmapSource = Bitmap.createBitmap(
         *                 pixels,
         *                 width,
         *                 height,
         *                 Bitmap.Config.ARGB_8888
         *         );
         *
         *         imageView.setImageBitmap(bitmapSource);
         *
         *         SlideHelper.addSeekbarLogic(
         *                 seekBar,
         *                 imageView,
         *                 mutableBoolean,
         *                 bitmapSource
         *         );
         *
         *         Bitmap bitmapAfterProgressChange;
         *         int[] pixelsAfterProgressChange = new int[width*height];
         *
         *         seekBar.setProgress(50);
         *         bitmapAfterProgressChange = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
         *         bitmapAfterProgressChange.getPixels(
         *                 pixelsAfterProgressChange,
         *                 0,
         *                 width,
         *                 0,
         *                 0,
         *                 width,
         *                 height
         *         );
         *         checkValuesColor(pixelsAfterProgressChange, 0, 49, Color.BLACK);
         *         checkValuesColor(pixelsAfterProgressChange, 50, 99, Color.TRANSPARENT);
         *
         *         seekBar.setProgress(100);
         *         bitmapAfterProgressChange = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
         *         bitmapAfterProgressChange.getPixels(
         *                 pixelsAfterProgressChange,
         *                 0,
         *                 width,
         *                 0,
         *                 0,
         *                 width,
         *                 height
         *         );
         *         checkValuesColor(pixelsAfterProgressChange, 0, 99, Color.BLACK);
         *
         *         seekBar.setProgress(1);
         *         bitmapAfterProgressChange = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
         *         bitmapAfterProgressChange.getPixels(
         *                 pixelsAfterProgressChange,
         *                 0,
         *                 width,
         *                 0,
         *                 0,
         *                 width,
         *                 height
         *         );
         *         checkValuesColor(pixelsAfterProgressChange, 0, 0, Color.BLACK);
         *         checkValuesColor(pixelsAfterProgressChange, 1, 99, Color.TRANSPARENT);
         *
         *         mutableBoolean.value = false;
         *
         *         seekBar.setProgress(50);
         *         bitmapAfterProgressChange = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
         *         bitmapAfterProgressChange.getPixels(
         *                 pixelsAfterProgressChange,
         *                 0,
         *                 width,
         *                 0,
         *                 0,
         *                 width,
         *                 height
         *         );
         *         checkValuesColor(pixelsAfterProgressChange, 0, 49, Color.TRANSPARENT);
         *         checkValuesColor(pixelsAfterProgressChange, 50, 99, Color.BLACK);
         *
         *         seekBar.setProgress(100);
         *         bitmapAfterProgressChange = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
         *         bitmapAfterProgressChange.getPixels(
         *                 pixelsAfterProgressChange,
         *                 0,
         *                 width,
         *                 0,
         *                 0,
         *                 width,
         *                 height
         *         );
         *         checkValuesColor(pixelsAfterProgressChange, 0, 99, Color.TRANSPARENT);
         *
         *         seekBar.setProgress(1);
         *         bitmapAfterProgressChange = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
         *         bitmapAfterProgressChange.getPixels(
         *                 pixelsAfterProgressChange,
         *                 0,
         *                 width,
         *                 0,
         *                 0,
         *                 width,
         *                 height
         *         );
         *         checkValuesColor(pixelsAfterProgressChange, 0, 0, Color.TRANSPARENT);
         *         checkValuesColor(pixelsAfterProgressChange, 1, 99, Color.BLACK);
         */
    }

    private void checkValuesColor(int[] pixels, int from, int until, int color)
    {
        for (int x = from; x <= until; x++) {
            for (int y = 0; y <= 99; y++) {
                assertEquals(
                        color,
                        pixels[x + (y*100)]
                );
            }
        }
    }
}
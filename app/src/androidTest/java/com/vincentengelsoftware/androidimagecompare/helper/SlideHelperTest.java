package com.vincentengelsoftware.androidimagecompare.helper;

import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;

import org.junit.Test;

public class SlideHelperTest {

    /**
     * Can be improved by dynamic values and check that ratio is fine
     */
    @Test
    public void resizeBitmap() {
        Bitmap bitmap;

        bitmap = getBitmap(5, 10);

        bitmap = SlideHelper.resizeBitmap(bitmap, 2, 5);
        assertEquals(2, bitmap.getWidth());
        assertEquals(4, bitmap.getHeight());

        bitmap = getBitmap(5, 10);

        bitmap = SlideHelper.resizeBitmap(bitmap, 5, 5);
        assertEquals(2, bitmap.getWidth(), 1);
        assertEquals(5, bitmap.getHeight());

        bitmap = getBitmap(10, 10);

        bitmap = SlideHelper.resizeBitmap(bitmap, 2, 5);
        assertEquals(2, bitmap.getWidth());
        assertEquals(2, bitmap.getHeight());

        bitmap = getBitmap(10, 10);

        bitmap = SlideHelper.resizeBitmap(bitmap, 5, 5);
        assertEquals(5, bitmap.getWidth());
        assertEquals(5, bitmap.getHeight());

        bitmap = getBitmap(10, 5);

        bitmap = SlideHelper.resizeBitmap(bitmap, 2, 5);
        assertEquals(2, bitmap.getWidth());
        assertEquals(1, bitmap.getHeight());

        bitmap = getBitmap(10, 5);

        bitmap = SlideHelper.resizeBitmap(bitmap, 5, 5);
        assertEquals(5, bitmap.getWidth());
        assertEquals(3, bitmap.getHeight(), 1);
    }

    private Bitmap getBitmap(int width, int height)
    {
        return Bitmap.createBitmap(
                new int[width*height],
                width,
                height,
                Bitmap.Config.ARGB_8888
        );
    }
}
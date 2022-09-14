package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

public class BitmapHelper {
    public static Bitmap resizeBitmap(Bitmap image, int maxWidth, int maxHeight) {
        int width = image.getWidth();
        int height = image.getHeight();

        float ratioBitmap = (float) width / (float) height;
        float ratioMax = (float) maxWidth / (float) maxHeight;

        int finalWidth = maxWidth;
        int finalHeight = maxHeight;

        if (ratioMax > ratioBitmap) {
            finalWidth = (int) ((float) maxHeight * ratioBitmap);
        } else {
            finalHeight = (int) ((float) maxWidth / ratioBitmap);
        }

        return Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree)
    {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(degree);

            return Bitmap.createBitmap(
                    bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true
            );
        } catch (Exception ignored) {}

        return bitmap;
    }

    public static Bitmap createTransparentBitmap(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        canvas.drawRect(0F, 0F, width, height, paint);
        return bitmap;
    }

    public static Bitmap getCutBitmap(Bitmap bitmap, int width, boolean keepFromLeft)
    {
        int fromX = 0;
        int toX = width;

        if (!keepFromLeft) {
            fromX = width;
            toX = bitmap.getWidth() - width;
        }

        return Bitmap.createBitmap(bitmap, fromX, 0, toX, bitmap.getHeight());
    }

    public static Bitmap mergeBitmap(Bitmap front, Bitmap back, int paddingLeft)
    {
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), front.getConfig());
        Canvas canvas = new Canvas(result);
        canvas.drawBitmap(back, 0f, 0f, null);
        canvas.drawBitmap(front, paddingLeft, 0f, null);

        return result;
    }

    public static Bitmap getCutBitmapWithTransparentBackgroundWithCanvas(
            Bitmap bitmap,
            Bitmap transparentBitmap,
            int width,
            boolean cutFromRightToLeft
    ) {
        Bitmap cutBitmap = BitmapHelper.getCutBitmap(
                bitmap,
                width,
                cutFromRightToLeft
        );

        if (cutFromRightToLeft) {
            width = 0;
        }

        return BitmapHelper.mergeBitmap(
                cutBitmap,
                transparentBitmap,
                width
        );
    }

    /**
     * Old bad way, don't use it
     */
    public static Bitmap getCutBitmapWithTransparentBackgroundWithArray(
            Bitmap bitmapSource,
            int width,
            boolean cutFromRightToLeft
    ) {
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

        if (cutFromRightToLeft) {
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

        return bitmapCopy;
    }
}

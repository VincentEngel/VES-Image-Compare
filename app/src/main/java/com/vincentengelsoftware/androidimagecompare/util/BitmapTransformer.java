package com.vincentengelsoftware.androidimagecompare.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import androidx.annotation.NonNull;

import com.vincentengelsoftware.androidimagecompare.domain.model.CropParams;

public class BitmapTransformer {

    /**
     * Reused across calls – configured once, never mutated again, so it is safe
     * to share between calls (Canvas reads paint properties but does not write them).
     */
    private static final Paint TRANSPARENT_PAINT;
    static {
        TRANSPARENT_PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
        TRANSPARENT_PAINT.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    public static Bitmap createScaledBitmapToMaxLength(Bitmap image, int maxWidth, int maxHeight) {
        try {
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
        } catch (Exception ignored) {}

        return image;
    }

    public static Bitmap resizeBitmap(Bitmap image, int width, int height) {
        try {
            return Bitmap.createScaledBitmap(image, width, height, true);
        } catch (Exception ignored) {}

        return image;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degree)
    {
        if (degree == 0) {
            return bitmap;
        }

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
        // Bitmap.createBitmap initialises all pixels to 0 (fully transparent for ARGB_8888);
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    private static Bitmap getCutBitmap(Bitmap bitmap, int width, boolean keepFromLeft)
    {
        int fromX = 0;
        int toX = width;

        if (!keepFromLeft) {
            fromX = width;
            toX = bitmap.getWidth() - width;
        }

        return Bitmap.createBitmap(bitmap, fromX, 0, toX, bitmap.getHeight());
    }

    private static Bitmap mergeBitmap(Bitmap front, Bitmap back, int paddingLeft)
    {
        Bitmap.Config config = front.getConfig() != null ? front.getConfig() : Bitmap.Config.ARGB_8888;
        Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), config);
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
        Bitmap cutBitmap = BitmapTransformer.getCutBitmap(
                bitmap,
                width,
                cutFromRightToLeft
        );

        if (cutFromRightToLeft) {
            width = 0;
        }

        return BitmapTransformer.mergeBitmap(
                cutBitmap,
                transparentBitmap,
                width
        );
    }

    /**
     * Applies the diagonal crop defined by {@code params} to {@code bitmapSource}.
     *
     * @param bitmapSource a mutable copy of the bitmap to cut (modified in-place)
     * @param params       the four seekbar states that define the crop geometry
     * @return the cropped bitmap (same instance as {@code bitmapSource})
     */
    public static Bitmap cutBitmapAny(@NonNull Bitmap bitmapSource, @NonNull CropParams params) {
        return cutBitmapAnyInternal(
                bitmapSource,
                params.topActive(),    params.topProgress(),
                params.leftActive(),   params.leftProgress(),
                params.rightActive(),  params.rightProgress(),
                params.bottomActive(), params.bottomProgress()
        );
    }

    private static Bitmap cutBitmapAnyInternal(
            Bitmap bitmapSource,
            boolean topSeekBarActive,
            int topSeekBarProgress,
            boolean leftSeekBarActive,
            int leftSeekBarProgress,
            boolean rightSeekBarActive,
            int rightSeekBarProgress,
            boolean bottomSeekBarActive,
            int bottomSeekBarProgress
    ) {
        return BitmapTransformer.getCutBitmapFromPoints(
                bitmapSource,
                BitmapTransformer.getPoints(
                        bitmapSource.getWidth(),
                        bitmapSource.getHeight(),
                        topSeekBarActive,
                        topSeekBarProgress,
                        leftSeekBarActive,
                        leftSeekBarProgress,
                        rightSeekBarActive,
                        rightSeekBarProgress,
                        bottomSeekBarActive,
                        bottomSeekBarProgress
                )
        );
    }

    private static Point[] getPoints(
            int width,
            int height,
            boolean topSeekBarActive,
            int topSeekBarProgress,
            boolean leftSeekBarActive,
            int leftSeekBarProgress,
            boolean rightSeekBarActive,
            int rightSeekBarProgress,
            boolean bottomSeekBarActive,
            int bottomSeekBarProgress
    )
    {
        // Corner Points are now created inline only in the branch that needs them,
        // avoiding 1-3 unnecessary heap allocations per call.
        Point[] points = new Point[5];
        int i = 0;

        if (topSeekBarActive && rightSeekBarActive) {
            points[i++] = new Point(width, (height * rightSeekBarProgress / 100));
            points[i++] = new Point((width * topSeekBarProgress / 100), 0);
            points[i]   = new Point(width, 0); // topright
            return points;
        }

        if (topSeekBarActive && bottomSeekBarActive) {
            points[i++] = new Point((width * bottomSeekBarProgress / 100), height);
            points[i++] = new Point((width * topSeekBarProgress / 100), 0);
            points[i++] = new Point(width, 0);    // topright
            points[i]   = new Point(width, height); // bottomright
            return points;
        }

        if (topSeekBarActive && leftSeekBarActive) {
            points[i++] = new Point(0, (height * leftSeekBarProgress / 100));
            points[i++] = new Point((width * topSeekBarProgress / 100), 0);
            points[i++] = new Point(width, 0);      // topright
            points[i++] = new Point(width, height);  // bottomright
            points[i]   = new Point(0, height);      // bottomleft
            return points;
        }

        if (rightSeekBarActive && bottomSeekBarActive) {
            points[i++] = new Point((width * bottomSeekBarProgress / 100), height);
            points[i++] = new Point(width, (height * rightSeekBarProgress / 100));
            points[i]   = new Point(width, height); // bottomright
            return points;
        }

        if (rightSeekBarActive && leftSeekBarActive) {
            points[i++] = new Point(0, (height * leftSeekBarProgress / 100));
            points[i++] = new Point(width, (height * rightSeekBarProgress / 100));
            points[i++] = new Point(width, height);  // bottomright
            points[i]   = new Point(0, height);      // bottomleft
            return points;
        }

        if (bottomSeekBarActive && leftSeekBarActive) {
            points[i++] = new Point(0, (height * leftSeekBarProgress / 100));
            points[i++] = new Point((width * bottomSeekBarProgress / 100), height);
            points[i]   = new Point(0, height); // bottomleft
            return points;
        }

        return points;
    }

    private static Bitmap getCutBitmapFromPoints(
            Bitmap bitmapSource,
            Point[] points
    ) {
        // No crop condition was active – skip path building and the try/catch entirely.
        if (points[0] == null) {
            return bitmapSource;
        }

        try {
            Path path = buildPath(points);
            Canvas canvas = new Canvas(bitmapSource);
            // Use the cached Paint instead of creating a new one on every call.
            canvas.drawPath(path, TRANSPARENT_PAINT);
            return bitmapSource;
        } catch (Exception ignored) {
        }

        return bitmapSource;
    }

    private static Path buildPath(Point[] points) {
        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);

        for (int i = 1; i < points.length; i++) {
            if (points[i] == null) {
                break;
            }
            path.lineTo(points[i].x, points[i].y);
        }

        // path.close() already draws a line back to the start point;
        path.close();

        return path;
    }
}

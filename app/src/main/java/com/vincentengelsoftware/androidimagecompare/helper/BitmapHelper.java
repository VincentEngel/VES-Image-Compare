package com.vincentengelsoftware.androidimagecompare.helper;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class BitmapHelper {
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

    public static Bitmap cutBitmapAny(
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
        return BitmapHelper.getCutBitmapFromPoints(
                bitmapSource,
                BitmapHelper.getPoints(
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

    public static Point[] getPoints(
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
        Point topright = new Point(width, 0);
        Point bottomleft = new Point(0, height);
        Point bottomright = new Point(width, height);

        Point[] points = new Point[5];
        int i = 0;

        if (topSeekBarActive && rightSeekBarActive) {
            points[i++] = new Point(width, (height * rightSeekBarProgress / 100));
            points[i++] = new Point((width * topSeekBarProgress / 100), 0);
            points[i] = topright;

            return points;
        }

        if (topSeekBarActive && bottomSeekBarActive) {
            points[i++] = new Point((width * bottomSeekBarProgress / 100), height);
            points[i++] = new Point((width * topSeekBarProgress / 100), 0);
            points[i++] = topright;
            points[i] = bottomright;

            return points;
        }

        if (topSeekBarActive && leftSeekBarActive) {
            points[i++] = new Point(0, (height * leftSeekBarProgress / 100));
            points[i++] = new Point((width * topSeekBarProgress / 100), 0);
            points[i++] = topright;
            points[i++] = bottomright;
            points[i] = bottomleft;

            return points;
        }

        if (rightSeekBarActive && bottomSeekBarActive) {
            points[i++] = new Point((width * bottomSeekBarProgress / 100), height);
            points[i++] = new Point(width, (height * rightSeekBarProgress / 100));
            points[i] = bottomright;

            return points;
        }

        if (rightSeekBarActive && leftSeekBarActive) {
            points[i++] = new Point(0, (height * leftSeekBarProgress / 100));
            points[i++] = new Point(width, (height * rightSeekBarProgress / 100));
            points[i++] = bottomright;
            points[i] = bottomleft;

            return points;
        }

        if (bottomSeekBarActive && leftSeekBarActive) {
            points[i++] = new Point(0, (height * leftSeekBarProgress / 100));
            points[i++] = new Point((width * bottomSeekBarProgress / 100), height);
            points[i] = bottomleft;

            return points;
        }


        return points;
    }

    public static Bitmap getCutBitmapFromPoints(
            Bitmap bitmapSource,
            Point[] points
    ) {
        try {
            Path path = BitmapHelper.getPathByPoints(points);

            Canvas canvas = new Canvas(bitmapSource);
            Paint transparentPaint = new Paint();
            transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            canvas.drawPath(path, transparentPaint);

            return bitmapSource;
        } catch (Exception ignored) {
        }

        return bitmapSource;
    }

    public static Path getPathByPoints(
            Point[] points
    ) {
        Path path = new Path();
        path.moveTo(points[0].x, points[0].y);

        for (int i = 1; i < points.length; i++) {
            if (points[i] == null) {
                break;
            }
            path.lineTo(points[i].x, points[i].y);
        }

        path.lineTo(points[0].x, points[0].y);

        path.close();

        return path;
    }
}

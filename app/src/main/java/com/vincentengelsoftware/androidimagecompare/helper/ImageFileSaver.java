package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Copies the raw bytes of an image identified by a content URI into a local cache file
 * without decoding the image into a Bitmap in memory.
 */
public class ImageFileSaver {

    /**
     * Copies the image at {@code sourceUri} into {@code destinationFile} at the byte level.
     *
     * @param contentResolver the ContentResolver used to open the source URI
     * @param sourceUri       the URI of the image to copy (e.g. picked from gallery)
     * @param destinationFile the local file to write the raw image bytes to
     * @return the Uri of the destination file on success, or {@code null} on failure
     */
    public static Uri saveUriToFile(ContentResolver contentResolver, Uri sourceUri, File destinationFile) {
        try (InputStream in = contentResolver.openInputStream(sourceUri);
             FileOutputStream fos = new FileOutputStream(destinationFile);
             FileChannel outChannel = fos.getChannel()) {

            if (in == null) {
                return null;
            }

            // NIO channel transfer — avoids copying bytes through a JVM heap buffer.
            // The OS moves data directly between the source and the destination file.
            try (ReadableByteChannel inChannel = Channels.newChannel(in)) {
                long position = 0;
                long transferred;
                do {
                    transferred = outChannel.transferFrom(inChannel, position, 1024 * 1024);
                    position += transferred;
                } while (transferred > 0);
            }

            return Uri.fromFile(destinationFile);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Compresses {@code bitmap} as lossless WebP into {@code destinationFile}.
     * <p>
     * Uses {@link Bitmap.CompressFormat#WEBP_LOSSLESS} on API 30+ and falls back to
     * {@link Bitmap.CompressFormat#WEBP} at quality 100 on API 28–29.
     * WebP lossless typically produces 25–35 % smaller files than PNG with faster encoding.
     *
     * @param bitmap          the bitmap to save
     * @param destinationFile the local file to write the compressed image into
     * @return the Uri of the destination file on success, or {@code null} on failure
     */
    public static Uri saveBitmapToFile(Bitmap bitmap, File destinationFile) {
        Bitmap.CompressFormat format = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                ? Bitmap.CompressFormat.WEBP_LOSSLESS
                : Bitmap.CompressFormat.WEBP;  // quality=100 → lossless on API 28-29

        try (FileOutputStream fos = new FileOutputStream(destinationFile);
             BufferedOutputStream out = new BufferedOutputStream(fos, 65536)) {
            // BufferedOutputStream (64 KB buffer) coalesces the WebP encoder's many small
            // writes into larger, fewer system calls before they reach the kernel.
            bitmap.compress(format, 100, out);
            out.flush();
            return Uri.fromFile(destinationFile);
        } catch (Exception ignored) {
            return null;
        }
    }
}


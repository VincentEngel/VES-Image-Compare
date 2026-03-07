package com.vincentengelsoftware.androidimagecompare.helper;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

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
    public static Uri saveToFile(ContentResolver contentResolver, Uri sourceUri, File destinationFile) {
        try (InputStream in = contentResolver.openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destinationFile)) {

            if (in == null) {
                return null;
            }

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.flush();

            return Uri.fromFile(destinationFile);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Compresses {@code bitmap} as PNG into {@code destinationFile}.
     *
     * @param bitmap          the bitmap to save
     * @param destinationFile the local file to write the compressed image into
     * @return the Uri of the destination file on success, or {@code null} on failure
     */
    public static Uri saveBitmapToFile(Bitmap bitmap, File destinationFile) {
        try (FileOutputStream out = new FileOutputStream(destinationFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            return Uri.fromFile(destinationFile);
        } catch (Exception ignored) {
            return null;
        }
    }
}


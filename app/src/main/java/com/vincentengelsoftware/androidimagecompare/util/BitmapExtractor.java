package com.vincentengelsoftware.androidimagecompare.util;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

public class BitmapExtractor {

    /**
     * Decodes a {@link Bitmap} from a content or file {@link Uri}.
     *
     * @param cr  the {@link ContentResolver} used to open the stream
     * @param uri the target URI
     * @return the decoded bitmap, or {@code null} if decoding failed
     */
    @Nullable
    public static Bitmap fromUri(@NonNull ContentResolver cr, @NonNull Uri uri) {
        try (InputStream input = cr.openInputStream(uri)) {
            return BitmapFactory.decodeStream(input);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Decodes a {@link Bitmap} from a URI string.
     * Supports both {@code file://} and content-provider URIs.
     *
     * @param cr        the {@link ContentResolver} used for content URIs
     * @param uriString a valid URI string
     * @return the decoded bitmap, or {@code null} if the string is invalid or
     *         decoding failed
     */
    @Nullable
    public static Bitmap fromUriString(@NonNull ContentResolver cr, @Nullable String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return null;
        }
        try {
            Uri uri = Uri.parse(uriString);
            if ("file".equals(uri.getScheme())) {
                return BitmapFactory.decodeFile(uri.getPath());
            }
            return fromUri(cr, uri);
        } catch (Exception ignored) {
            return null;
        }
    }
}

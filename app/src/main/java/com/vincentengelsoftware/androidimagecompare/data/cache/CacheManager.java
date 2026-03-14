package com.vincentengelsoftware.androidimagecompare.data.cache;

import android.net.Uri;

import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Centralises all cache-file names and lifecycle management for the app's image cache.
 * <p>
 * Provides factory methods that build {@link File} references from a cache directory, and
 * a {@link #cleanup} method that prunes every cached file that is no longer referenced by
 * the current {@link ImageSessionState}.
 */
public class CacheManager {

    /** Output file for the left (first) processed compare image. */
    public static final String COMPARE_FILE_ONE  = "compare_image_one.png";
    /** Output file for the right (second) processed compare image. */
    public static final String COMPARE_FILE_TWO  = "compare_image_two.png";
    /** Temporary file that the camera app writes its raw capture into. */
    public static final String CAMERA_TEMP_FILE  = "camera_capture_temp.jpg";

    public static File getCompareFileOne(File cacheDir) {
        return new File(cacheDir, COMPARE_FILE_ONE);
    }

    public static File getCompareFileTwo(File cacheDir) {
        return new File(cacheDir, COMPARE_FILE_TWO);
    }

    public static File getCameraTempFile(File cacheDir) {
        return new File(cacheDir, CAMERA_TEMP_FILE);
    }

    /**
     * Deletes every file in {@code cacheDir} whose canonical path is not referenced by the
     * current left or right image URI in {@code sessionState}.
     * Safe to call from {@code Activity.onDestroy}.
     */
    public static void cleanup(File cacheDir, ImageSessionState sessionState) {
        try {
            File[] files = cacheDir.listFiles();
            if (files == null) return;

            Set<String> keepPaths = buildKeepPaths(sessionState);
            for (File file : files) {
                try {
                    if (!keepPaths.contains(file.getCanonicalPath())) {
                        file.delete();
                    }
                } catch (IOException ignored) {}
            }
        } catch (Exception ignored) {}
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private static Set<String> buildKeepPaths(ImageSessionState state) throws IOException {
        Set<String> paths = new HashSet<>();
        addUriPath(paths, state.getLeftImageUri());
        addUriPath(paths, state.getRightImageUri());
        return paths;
    }

    private static void addUriPath(Set<String> paths, Uri uri) throws IOException {
        if (uri != null && uri.getPath() != null) {
            paths.add(new File(uri.getPath()).getCanonicalPath());
        }
    }
}


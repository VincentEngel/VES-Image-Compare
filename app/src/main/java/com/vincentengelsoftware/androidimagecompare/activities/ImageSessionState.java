package com.vincentengelsoftware.androidimagecompare.activities;

import android.net.Uri;

import com.vincentengelsoftware.androidimagecompare.util.imageInformation.ImageInfoHolder;

/**
 * Singleton that owns the two image slots (left / right) for the main comparison screen.
 * <p>
 * Centralises the URIs and {@link ImageInfoHolder} references that were previously
 * scattered as {@code public static} fields on {@link MainActivity}, removing the
 * global-mutable-state anti-pattern from the Activity class.
 */
public class ImageSessionState {

    /** Bundle key used to persist and restore the left-slot URI across process death. */
    public static final String LEFT_URI_KEY  = "leftImageUriKey";
    /** Bundle key used to persist and restore the right-slot URI across process death. */
    public static final String RIGHT_URI_KEY = "rightImageUriKey";

    private static ImageSessionState instance;

    // volatile so background threads that read the URI always see the latest value.
    private volatile Uri leftImageUri;
    private volatile Uri rightImageUri;

    private final ImageInfoHolder firstImageInfoHolder  = new ImageInfoHolder();
    private final ImageInfoHolder secondImageInfoHolder = new ImageInfoHolder();

    private ImageSessionState() {}

    public static synchronized ImageSessionState getInstance() {
        if (instance == null) {
            instance = new ImageSessionState();
        }
        return instance;
    }

    // ── left slot ────────────────────────────────────────────────────────────

    public Uri getLeftImageUri()         { return leftImageUri; }
    public void setLeftImageUri(Uri uri) { this.leftImageUri = uri; }

    // ── right slot ───────────────────────────────────────────────────────────

    public Uri getRightImageUri()         { return rightImageUri; }
    public void setRightImageUri(Uri uri) { this.rightImageUri = uri; }

    // ── image info holders ────────────────────────────────────────────────────

    public ImageInfoHolder getFirstImageInfoHolder()  { return firstImageInfoHolder; }
    public ImageInfoHolder getSecondImageInfoHolder() { return secondImageInfoHolder; }

    /**
     * Atomically swaps the left and right URIs.
     * Called by the swap-images action after the {@link ImageInfoHolder}s have already
     * been swapped in-place by
     * {@link com.vincentengelsoftware.androidimagecompare.helper.MainHelper#addSwapImageLogic}.
     */
    public void swapUris() {
        Uri temp       = leftImageUri;
        leftImageUri   = rightImageUri;
        rightImageUri  = temp;
    }
}


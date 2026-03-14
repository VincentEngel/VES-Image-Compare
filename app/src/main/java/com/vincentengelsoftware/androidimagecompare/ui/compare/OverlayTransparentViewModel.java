package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link OverlayTransparentActivity}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Retains the decoded bitmaps across rotation so they are never re-decoded.</li>
 *   <li>Owns the {@link #sync} zoom-link flag so it persists across rotation.</li>
 * </ul>
 * </p>
 */
public class OverlayTransparentViewModel extends ViewModel {

    // ── Bitmaps (survive rotation) ─────────────────────────────────────────────

    /** The base (background) image – decoded once on first launch, never mutated. */
    @Nullable private Bitmap bitmapBase;

    /** The front (transparent) image – decoded once on first launch, never mutated. */
    @Nullable private Bitmap bitmapTransparent;

    /**
     * Returns {@code true} when bitmaps have already been loaded (i.e. after a
     * configuration change) so the Activity does not decode them again.
     */
    public boolean areBitmapsLoaded() {
        return bitmapBase != null && bitmapTransparent != null;
    }

    /**
     * Stores the decoded bitmaps. Must be called exactly once, on the Activity's
     * first launch.
     *
     * @param bitmapBase        the background image
     * @param bitmapTransparent the front image whose opacity is controlled by the seekbar
     */
    public void initBitmaps(@NonNull Bitmap bitmapBase, @NonNull Bitmap bitmapTransparent) {
        this.bitmapBase        = bitmapBase;
        this.bitmapTransparent = bitmapTransparent;
    }

    /** Returns the base image, or {@code null} before {@link #initBitmaps} is called. */
    @Nullable
    public Bitmap getBitmapBase() {
        return bitmapBase;
    }

    /** Returns the front image, or {@code null} before {@link #initBitmaps} is called. */
    @Nullable
    public Bitmap getBitmapTransparent() {
        return bitmapTransparent;
    }

    // ── Shared mutable state ───────────────────────────────────────────────────

    /**
     * Whether zoom/pan state is synchronised between both image views.
     * Initialised to {@code true}; updated from the Intent on first launch and
     * from {@code savedInstanceState} on process-death restoration.
     */
    private final AtomicBoolean sync = new AtomicBoolean(true);

    /** Returns the shared sync flag used by {@link SyncZoom}. */
    public AtomicBoolean getSync() {
        return sync;
    }
}


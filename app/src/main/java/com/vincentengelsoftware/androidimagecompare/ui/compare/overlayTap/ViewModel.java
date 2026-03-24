package com.vincentengelsoftware.androidimagecompare.ui.compare.overlayTap;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link OverlayTapActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Owns the {@link #sync} zoom-link flag so it persists across rotation.
 * </ul>
 *
 * <p>Images are loaded directly from their URIs on each (re-)creation of the Activity; no bitmap is
 * retained in memory here.
 */
public class ViewModel extends androidx.lifecycle.ViewModel {

  // ── Shared mutable state ───────────────────────────────────────────────────

  /**
   * Whether zoom/pan state is copied to the other image view when the user taps to switch images.
   * Initialised to {@code true}; updated from the Intent on first launch and from {@code
   * savedInstanceState} on rotation.
   */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  /**
   * Returns the shared sync flag used by {@link
   * com.vincentengelsoftware.androidimagecompare.ui.compare.shared.TapHelper} and {@link
   * com.vincentengelsoftware.androidimagecompare.ui.compare.shared.SyncZoom}.
   */
  public AtomicBoolean getSync() {
    return sync;
  }
}

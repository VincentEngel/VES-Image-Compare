package com.vincentengelsoftware.androidimagecompare.ui.compare;

import androidx.lifecycle.ViewModel;
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
public class OverlayTapViewModel extends ViewModel {

  // ── Shared mutable state ───────────────────────────────────────────────────

  /**
   * Whether zoom/pan state is copied to the other image view when the user taps to switch images.
   * Initialised to {@code true}; updated from the Intent on first launch and from {@code
   * savedInstanceState} on rotation.
   */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  /** Returns the shared sync flag used by {@link TapHelper} and {@link SyncZoom}. */
  public AtomicBoolean getSync() {
    return sync;
  }
}

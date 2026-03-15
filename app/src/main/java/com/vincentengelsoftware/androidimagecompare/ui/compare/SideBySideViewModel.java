package com.vincentengelsoftware.androidimagecompare.ui.compare;

import androidx.lifecycle.ViewModel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Survives configuration changes (e.g. screen rotation) for {@link SideBySideActivity}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Owns the {@link #getSync()} zoom-link flag so it is never reset on a configuration change.
 * </ul>
 *
 * <p>Images are loaded directly from their URIs on each (re-)creation of the Activity; no bitmap
 * is retained in memory here.
 */
public class SideBySideViewModel extends ViewModel {

  // ── Shared sync state ──────────────────────────────────────────────────────

  /**
   * Whether both image views pan/zoom together. Initialised from the launching Intent on first
   * launch; retained by the ViewModel on subsequent configuration changes.
   */
  private final AtomicBoolean sync = new AtomicBoolean(true);

  public AtomicBoolean getSync() {
    return sync;
  }
}

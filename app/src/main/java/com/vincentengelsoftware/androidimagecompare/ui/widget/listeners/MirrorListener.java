package com.vincentengelsoftware.androidimagecompare.ui.widget.listeners;

import android.view.MotionEvent;
import android.view.View;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.ui.widget.VesImageInterface;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Forwards touch events from a source image view to a target image view, implementing the selected
 * mirroring strategy.
 *
 * <p>A shared {@code disabled} flag breaks the feedback loop that would otherwise occur when two
 * views each hold a {@code MirrorListener} pointing at the other.
 */
public class MirrorListener implements OnTouchListenerInterface {

  private final VesImageInterface source;
  private final VesImageInterface target;
  private final AtomicBoolean sync;
  private final AtomicBoolean disabled;
  private final int mirroringType;

  /**
   * @param source the view that owns this listener
   * @param target the view that will receive mirrored events
   * @param sync shared flag; mirroring is skipped when {@code false}
   * @param disabled shared re-entrancy guard; set to {@code true} while forwarding
   * @param mirroringType the mirroring strategy to apply on each touch event
   */
  public MirrorListener(
      VesImageInterface source,
      VesImageInterface target,
      AtomicBoolean sync,
      AtomicBoolean disabled,
      int mirroringType) {
    this.source = source;
    this.target = target;
    this.sync = sync;
    this.disabled = disabled;
    this.mirroringType = mirroringType;
  }

  @Override
  public void trigger(View view, MotionEvent motionEvent) {
    if (disabled.get() || !sync.get()) return;
    handleMirroring(motionEvent);
  }

  private void handleMirroring(MotionEvent motionEvent) {
    if (mirroringType == Status.NATURAL_MIRRORING) {
      disabled.set(true);
      target.triggerOnTouchEvent(motionEvent);
      disabled.set(false);

    } else if (mirroringType == Status.STRICT_MIRRORING) {
      target.setImageScaleCenter(source.getImageScaleCenter());

    } else if (mirroringType == Status.LOOSE_MIRRORING) {
      target.setImageScale(source.getImageScaleCenter());
    }
  }
}

package com.vincentengelsoftware.androidimagecompare.ui.widget.listeners;

import android.view.MotionEvent;
import android.view.View;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;

/** Forwards touch events to the host Activity to show the controls bar. */
public class FadeListener implements OnTouchListenerInterface {

  private final ControlsBarHost controlsBarHost;

  public FadeListener(ControlsBarHost controlsBarHost) {
    this.controlsBarHost = controlsBarHost;
  }

  @Override
  public void trigger(View view, MotionEvent motionEvent) {
    controlsBarHost.showControlsBar();
  }
}

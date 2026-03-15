package com.vincentengelsoftware.androidimagecompare.ui.animation;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import java.util.concurrent.atomic.AtomicBoolean;

/** Animates a {@link View}'s height or width between its current size and a target size. */
public class ResizeAnimation extends Animation {

  /** The dimension this animation resizes. */
  public enum Dimension {
    HEIGHT,
    WIDTH
  }

  /** Whether this animation reveals or conceals the view. */
  public enum AnimationMode {
    SHOW,
    HIDE
  }

  /** Delay (ms) before the controls bar auto-hides after the last interaction. */
  public static final int DURATION_LONG = 2000;

  /** Duration (ms) of the show/hide slide animation itself. */
  public static final int DURATION_SHORT = 500;

  private final View view;
  private final int targetSize;
  private final int startSize;
  private final Dimension dimension;

  public ResizeAnimation(
      View view,
      int targetSize,
      Dimension dimension,
      AnimationMode mode,
      AtomicBoolean continueHiding) {
    this.view = view;
    this.targetSize = targetSize;
    this.dimension = dimension;
    this.startSize = dimension == Dimension.HEIGHT ? view.getHeight() : view.getWidth();

    setAnimationListener(
        new AnimationListener() {
          @Override
          public void onAnimationStart(Animation animation) {}

          @Override
          public void onAnimationEnd(Animation animation) {
            try {
              if (mode == AnimationMode.HIDE) {
                if (continueHiding.get()) {
                  view.setVisibility(View.INVISIBLE);
                }
              } else {
                view.setVisibility(View.VISIBLE);
              }
            } catch (Exception ignored) {
            }
          }

          @Override
          public void onAnimationRepeat(Animation animation) {}
        });
  }

  @Override
  protected void applyTransformation(float interpolatedTime, Transformation t) {
    int size = startSize + (int) ((targetSize - startSize) * interpolatedTime);
    if (dimension == Dimension.HEIGHT) {
      view.getLayoutParams().height = size;
    } else {
      view.getLayoutParams().width = size;
    }
    view.requestLayout();
  }

  @Override
  public void initialize(int width, int height, int parentWidth, int parentHeight) {
    super.initialize(width, height, parentWidth, parentHeight);
  }
}

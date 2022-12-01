package com.vincentengelsoftware.androidimagecompare.animations;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class ResizeAnimation extends Animation {
    private final int targetSize;
    private final int startSize;
    private final View view;
    private final boolean targetDimension;

    public final static boolean CHANGE_WIDTH = true;
    public final static boolean CHANGE_HEIGHT = false;
    public final static int DURATION_SHORT = 500;
    public final static boolean IS_HIDING_ANIMATION = true;
    public final static boolean IS_SHOWING_ANIMATION = false;

    public ResizeAnimation(
            View view,
            int targetSize,
            boolean targetDimension,
            boolean isHidingAnimation,
            UtilMutableBoolean mutableBoolean
    ) {
        this.view = view;
        this.targetSize = targetSize;
        this.targetDimension = targetDimension;
        this.startSize = view.getHeight();

        this.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                try {
                    if (isHidingAnimation) {
                        if (mutableBoolean.value) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        view.setVisibility(View.VISIBLE);
                    }
                } catch (Exception ignored) {
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int size = startSize + (int) ((targetSize - startSize) * interpolatedTime);

        if (this.targetDimension == ResizeAnimation.CHANGE_HEIGHT) {
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

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}

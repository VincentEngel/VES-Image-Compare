package com.vincentengelsoftware.androidimagecompare.viewClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentengelsoftware.androidimagecompare.SideBySideActivity;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class ZoomImageView extends SubsamplingScaleImageView implements VesImageInterface {
    private VesImageInterface linkedTarget;
    private UtilMutableBoolean sync;

    private FadeActivity fadeActivity;

    public void setLinkedTarget(VesImageInterface linkedTarget, UtilMutableBoolean sync) {
        this.linkedTarget = linkedTarget;
        this.sync = sync;
    }

    public void setFadeActivity(FadeActivity fadeActivity)
    {
        this.fadeActivity = fadeActivity;
    }

    public ZoomImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ZoomImageView(Context context) {
        super(context);
    }

    @Override
    public void setBitmapImage(Bitmap bitmap) {
        super.setImage(ImageSource.bitmap(bitmap));
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (fadeActivity != null) {
            fadeActivity.triggerFadeIn();
        }
        if (sync != null && sync.value && linkedTarget != null) {
            linkedTarget.triggerLinkedTargetTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void applyScaleAndCenter(SubsamplingScaleImageView imageView) {
        super.setScaleAndCenter(imageView.getScale(), imageView.getCenter());
    }

    public void triggerLinkedTargetTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
    }
}

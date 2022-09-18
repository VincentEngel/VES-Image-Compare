package com.vincentengelsoftware.androidimagecompare.viewClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.vincentengelsoftware.androidimagecompare.SideBySideActivity;

public class ZoomImageView extends SubsamplingScaleImageView implements VesImageInterface {
    private VesImageInterface linkedTarget;

    public void setLinkedTarget(VesImageInterface linkedTarget) {
        this.linkedTarget = linkedTarget;
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
        if (SideBySideActivity.sync && linkedTarget != null) {
            linkedTarget.triggerLinkedTargetTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public void triggerLinkedTargetTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
    }
}

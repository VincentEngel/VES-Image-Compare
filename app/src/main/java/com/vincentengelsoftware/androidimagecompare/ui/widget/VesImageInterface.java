package com.vincentengelsoftware.androidimagecompare.ui.widget;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;

import java.util.concurrent.atomic.AtomicBoolean;

public interface VesImageInterface {
    void setBitmapImage(Bitmap bitmap);
    void setVisibility(int visibility);
    void setOnClickListener(View.OnClickListener l);
    ViewGroup getParentViewGroup();
    void setAlpha(float alpha);
    void resetZoom();
    void applyScaleAndCenter(VesImageInterface imageView);
    ImageScaleCenter getImageScaleCenter();
    void setImageScaleCenter(ImageScaleCenter imageScaleCenter);
    void addFadeListener(ControlsBarHost controlsBarHost);
    void addMirrorListener(VesImageInterface target, AtomicBoolean sync, AtomicBoolean disabled);
    void setImageScale(ImageScaleCenter imageScaleCenter);
    void triggerOnTouchEvent(MotionEvent event);
    Bitmap getCurrentBitmap();
}

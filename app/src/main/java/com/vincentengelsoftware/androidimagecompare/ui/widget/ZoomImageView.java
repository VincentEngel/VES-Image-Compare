package com.vincentengelsoftware.androidimagecompare.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.ortiz.touchview.TouchImageView;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.FadeListener;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.MirrorListener;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.OnTouchListenerInterface;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.OnTouchListeners;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;
import com.vincentengelsoftware.androidimagecompare.constants.Settings;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link TouchImageView} that adds mirror-listener and fade-listener support
 * on top of standard touch zoom/pan, and implements {@link VesImageInterface}.
 */
public class ZoomImageView extends TouchImageView implements VesImageInterface {

    private final OnTouchListeners onTouchListeners = new OnTouchListeners();

    public ZoomImageView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public ZoomImageView(Context context) {
        super(context);
    }

    /**
     * Applies zoom limits from {@link Settings} after the view is attached to its window.
     * {@link Settings#init} is guaranteed to have been called by this point (it runs
     * during Application start-up, before any Activity window is created).
     */
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        super.setMaxZoom(Settings.MAX_ZOOM);
        super.setMinZoom(Settings.MIN_ZOOM);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addOnTouchEventListener(OnTouchListenerInterface touchListener) {
        onTouchListeners.add(touchListener);
        super.setOnTouchListener(this.onTouchListeners);
    }

    @Override
    public void addMirrorListener(VesImageInterface target, AtomicBoolean sync, AtomicBoolean disabled) {
        this.addOnTouchEventListener(new MirrorListener(this, target, sync, disabled));
    }

    @Override
    public void addFadeListener(ControlsBarHost controlsBarHost) {
        this.addOnTouchEventListener(new FadeListener(controlsBarHost));
    }

    @Override
    public void setBitmapImage(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
    }

    @Override
    public void applyScaleAndCenter(VesImageInterface imageView) {
        super.setZoom((TouchImageView) imageView);
    }

    @Override
    public void setImageScaleCenter(ImageScaleCenter imageScaleCenter) {
        super.setZoom(imageScaleCenter.scale(), imageScaleCenter.centerX(), imageScaleCenter.centerY());
    }

    @Override
    public void setImageScale(ImageScaleCenter imageScaleCenter) {
        PointF scrollPosition = super.getScrollPosition();
        super.setZoom(imageScaleCenter.scale(), scrollPosition.x, scrollPosition.y);
    }

    @Override
    public ImageScaleCenter getImageScaleCenter() {
        PointF scrollPosition = super.getScrollPosition();
        return new ImageScaleCenter(super.getCurrentZoom(), scrollPosition.x, scrollPosition.y);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void triggerOnTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
    }

    @Override
    public ViewGroup getParentViewGroup() {
        return (ViewGroup) super.getParent();
    }

    @Override
    public Bitmap getCurrentBitmap() {
        return ((BitmapDrawable) super.getDrawable()).getBitmap();
    }
}

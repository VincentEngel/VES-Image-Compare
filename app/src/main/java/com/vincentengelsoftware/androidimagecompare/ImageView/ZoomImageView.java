package com.vincentengelsoftware.androidimagecompare.ImageView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import com.ortiz.touchview.TouchImageView;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.FadeListener;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.MirrorListener;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.OnTouchListenerInterface;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.OnTouchListeners;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;

import java.util.concurrent.atomic.AtomicBoolean;

public class ZoomImageView extends TouchImageView implements VesImageInterface {
    private final OnTouchListeners onTouchListeners = new OnTouchListeners();

    @SuppressLint("ClickableViewAccessibility")
    private void addOnTouchEventListener(OnTouchListenerInterface touchListener) {
        onTouchListeners.add(touchListener);

        super.setOnTouchListener(this.onTouchListeners);
    }

    public void addMirrorListener(VesImageInterface target, AtomicBoolean sync, AtomicBoolean disabled)
    {
        this.addOnTouchEventListener(new MirrorListener(this, target, sync, disabled));
    }

    public void addFadeListener(FadeActivity fadeActivity)
    {
        this.addOnTouchEventListener(new FadeListener(fadeActivity));
    }

    public ZoomImageView(Context context, AttributeSet attr) {
        super(context, attr);
        super.setMaxZoom(Settings.MAX_ZOOM); // Bad practice
        super.setMinZoom(Settings.MIN_ZOOM); // Bad practice
    }

    public ZoomImageView(Context context) {
        super(context);
        super.setMaxZoom(Settings.MAX_ZOOM); // Bad practice
        super.setMinZoom(Settings.MIN_ZOOM); // Bad practice
    }

    @Override
    public void setBitmapImage(Bitmap bitmap) {
        super.setImageBitmap(bitmap);
    }

    public void applyScaleAndCenter(VesImageInterface imageView) {
        super.setZoom((TouchImageView) imageView);
    }

    public void setImageScaleCenter(ImageScaleCenter imageScaleCenter) {
        super.setZoom(imageScaleCenter.getScale(), imageScaleCenter.getCenterX(), imageScaleCenter.getCenterY());
    }

    public void setImageScale(ImageScaleCenter imageScaleCenter) {
        PointF scrollPosition = super.getScrollPosition();
        super.setZoom(imageScaleCenter.getScale(), scrollPosition.x, scrollPosition.y);
    }

    public ImageScaleCenter getImageScaleCenter() {
        PointF scrollPosition = super.getScrollPosition();
        return new ImageScaleCenter(super.getCurrentZoom(), scrollPosition.x, scrollPosition.y);
    }

    @SuppressLint("ClickableViewAccessibility")
    public void triggerOnTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
    }

    public ViewGroup getParentViewGroup() {
        return (ViewGroup) super.getParent();
    }

    public Bitmap getCurrentBitmap() {
        return ((BitmapDrawable)super.getDrawable()).getBitmap();
    }
}

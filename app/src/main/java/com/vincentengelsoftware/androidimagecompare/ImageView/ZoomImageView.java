package com.vincentengelsoftware.androidimagecompare.ImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.util.AttributeSet;
import com.ortiz.touchview.TouchImageView;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.FadeListener;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.MirrorListener;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.OnTouchListenerInterface;
import com.vincentengelsoftware.androidimagecompare.ImageView.Listeners.OnTouchListeners;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.globals.Settings;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class ZoomImageView extends TouchImageView implements VesImageInterface {
    private final OnTouchListeners onTouchListeners = new OnTouchListeners();

    private void addOnTouchEventListener(OnTouchListenerInterface touchListener) {
        onTouchListeners.add(touchListener);

        super.setOnTouchImageViewListener(
                this.onTouchListeners::trigger
        );
    }

    public void addMirrorListener(VesImageInterface target, UtilMutableBoolean sync)
    {
        this.addOnTouchEventListener(new MirrorListener(this, target, sync));
    }

    public void addFadeListener(FadeActivity fadeActivity)
    {
        this.addOnTouchEventListener(new FadeListener(fadeActivity));
    }

    public ZoomImageView(Context context, AttributeSet attr) {
        super(context, attr);
        super.setMaxZoom(Settings.MAX_ZOOM); // Bad practice
    }

    public ZoomImageView(Context context) {
        super(context);
        super.setMaxZoom(Settings.MAX_ZOOM); // Bad practice
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
}

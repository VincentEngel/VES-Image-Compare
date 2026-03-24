package com.vincentengelsoftware.androidimagecompare.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import com.ortiz.touchview.OnTouchImageViewListener;
import com.ortiz.touchview.TouchImageView;
import com.vincentengelsoftware.androidimagecompare.ui.animation.ControlsBarHost;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.FadeListener;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.MirrorListener;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.OnTouchListenerInterface;
import com.vincentengelsoftware.androidimagecompare.ui.widget.listeners.OnTouchListeners;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link TouchImageView} that adds mirror-listener and fade-listener support on top of standard
 * touch zoom/pan, and implements {@link VesImageInterface}.
 */
public class ZoomImageView extends TouchImageView implements VesImageInterface {

  private final OnTouchListeners onTouchListeners = new OnTouchListeners();

  /** Listeners notified via {@link OnTouchImageViewListener#onMove()} on every zoom/pan event. */
  private final List<Runnable> zoomChangeListeners = new ArrayList<>();

  public ZoomImageView(Context context, AttributeSet attr) {
    super(context, attr);
  }

  public ZoomImageView(Context context) {
    super(context);
  }

  @Override
  public void initZoomLimits(int maxZoom, float minZoom) {
    super.setMaxZoom(maxZoom);
    super.setMinZoom(minZoom);
  }

  @SuppressLint("ClickableViewAccessibility")
  private void addOnTouchEventListener(OnTouchListenerInterface touchListener) {
    onTouchListeners.add(touchListener);
    super.setOnTouchListener(this.onTouchListeners);
  }

  @Override
  public void addMirrorListener(
      VesImageInterface target, AtomicBoolean sync, AtomicBoolean disabled, int mirroringType) {
    this.addOnTouchEventListener(new MirrorListener(this, target, sync, disabled, mirroringType));
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
  public void setImageURI(Uri uri) {
    super.setImageURI(uri);
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

  /**
   * Registers a listener that is called whenever the image is panned or zoomed by the user.
   *
   * <p>Multiple listeners are supported. Internally they share a single {@link
   * OnTouchImageViewListener} slot provided by {@link TouchImageView}.
   *
   * @param onZoomChanged callback invoked on every zoom/pan change
   */
  public void addZoomChangeListener(Runnable onZoomChanged) {
    if (zoomChangeListeners.isEmpty()) {
      super.setOnTouchImageViewListener(
          new OnTouchImageViewListener() {
            @Override
            public void onMove() {
              for (Runnable listener : zoomChangeListeners) {
                listener.run();
              }
            }
          });
    }
    zoomChangeListeners.add(onZoomChanged);
  }
}

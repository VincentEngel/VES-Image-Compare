package com.vincentengelsoftware.androidimagecompare.ImageView;

import android.graphics.Bitmap;
import android.view.View;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public interface VesImageInterface {
    void setBitmapImage(Bitmap bitmap);
    int getVisibility();
    void setVisibility(int visibility);
    void setOnClickListener(View.OnClickListener l);
    void bringToFront();
    void setAlpha(float alpha);

    void resetZoom();
    void applyScaleAndCenter(VesImageInterface imageView);

    ImageScaleCenter getImageScaleCenter();
    void setImageScaleCenter(ImageScaleCenter imageScaleCenter);

    void addFadeListener(FadeActivity fadeActivity);
    void addMirrorListener(VesImageInterface target, UtilMutableBoolean sync);

    void setImageScale(ImageScaleCenter imageScaleCenter);
}

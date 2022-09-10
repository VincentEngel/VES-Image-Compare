package com.vincentengelsoftware.androidimagecompare.viewClasses;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class ZoomImageView extends SubsamplingScaleImageView implements VesImageInterface {
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
}

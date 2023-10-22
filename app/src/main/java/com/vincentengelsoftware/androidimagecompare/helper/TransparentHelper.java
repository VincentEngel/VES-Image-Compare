package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageButton;

import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

import com.vincentengelsoftware.androidimagecompare.R;

public class TransparentHelper {
    public static void makeTargetTransparent(SeekBar seekBar, VesImageInterface imageView, ImageButton hideShow, FadeActivity activity)
    {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                activity.instantFadeIn();
                if (progress <= 2) {
                    hideShow.setImageResource(R.drawable.ic_visibility_off);
                    imageView.setVisibility(View.GONE);
                    activity.triggerFadeOutThread();
                    return;
                }

                imageView.setAlpha((float) progress / (float) seekBar.getMax());

                hideShow.setImageResource(R.drawable.ic_visibility);
                imageView.setVisibility(View.VISIBLE);
                activity.triggerFadeOutThread();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}

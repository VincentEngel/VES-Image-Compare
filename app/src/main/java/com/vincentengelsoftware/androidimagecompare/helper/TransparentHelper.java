package com.vincentengelsoftware.androidimagecompare.helper;

import android.widget.ImageView;
import android.widget.SeekBar;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class TransparentHelper {
    public static void makeTargetTransparent(SeekBar seekBar, SubsamplingScaleImageView imageView)
    {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                imageView.setAlpha(
                        (float) seekBar.getProgress() / (float) seekBar.getMax()
                );
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                imageView.setAlpha(
                        (float) seekBar.getProgress() / (float) seekBar.getMax()
                );
            }
        });
    }
}

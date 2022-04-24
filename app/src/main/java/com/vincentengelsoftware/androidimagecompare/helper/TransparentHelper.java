package com.vincentengelsoftware.androidimagecompare.helper;

import android.widget.ImageView;
import android.widget.SeekBar;

public class TransparentHelper {
    public static void makeTargetTransparent(SeekBar seekBar, ImageView imageView)
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

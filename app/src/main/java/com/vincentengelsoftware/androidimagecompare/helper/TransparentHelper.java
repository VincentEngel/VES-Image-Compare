package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.SeekBar;
import android.widget.ImageButton;

import com.vincentengelsoftware.androidimagecompare.viewClasses.VesImageInterface;

import com.vincentengelsoftware.androidimagecompare.R;

public class TransparentHelper {
    public static void makeTargetTransparent(SeekBar seekBar, VesImageInterface imageView, ImageButton hideShow)
    {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (seekBar.getProgress() <= 2) {
                    imageView.setVisibility(View.GONE);
                } else {
                    hideShow.setImageResource(R.drawable.ic_visibility_vector);
                    imageView.setVisibility(View.VISIBLE);
                }
                imageView.setAlpha((float) seekBar.getProgress() / (float) seekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}

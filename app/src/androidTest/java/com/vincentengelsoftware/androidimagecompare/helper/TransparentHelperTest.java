package com.vincentengelsoftware.androidimagecompare.helper;

import static org.junit.Assert.*;

import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;

public class TransparentHelperTest {

    @Test
    public void makeTargetTransparent() {
        SeekBar seekBar = new SeekBar(ApplicationProvider.getApplicationContext());
        ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());

        seekBar.setProgress(50);
        imageView.setAlpha((float) 0.5);

        TransparentHelper.makeTargetTransparent(seekBar, imageView);

        seekBar.setProgress(0);

        assertEquals((float) 0, imageView.getAlpha(), 0);

        seekBar.setProgress(1);

        assertEquals((float) 0.01, imageView.getAlpha(), 0);

        seekBar.setProgress(100);
        assertEquals((float) 1.0, imageView.getAlpha(), 0);
    }
}
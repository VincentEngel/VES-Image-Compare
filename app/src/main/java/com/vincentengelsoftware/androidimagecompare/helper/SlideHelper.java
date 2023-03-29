package com.vincentengelsoftware.androidimagecompare.helper;

import android.widget.ImageButton;
import android.widget.SeekBar;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;

public class SlideHelper {
    public static void setSwapSlideDirectionOnClick(
            ImageButton imageButton,
            SeekBar seekBar,
            UtilMutableBoolean mutableBoolean,
            FadeActivity activity
    ) {
        imageButton.setOnClickListener(view -> {
            activity.instantFadeIn();
            mutableBoolean.value = !mutableBoolean.value;

            if (mutableBoolean.value) {
                imageButton.setImageResource(R.drawable.ic_slide_ltr);
            } else {
                imageButton.setImageResource(R.drawable.ic_slide_rtl);
            }

            int progress = 50;
            // onProgressChanged is not triggered if setProgress is called with current progress
            if (seekBar.getProgress() == progress) {
                progress = 51;
            }
            seekBar.setProgress(progress);
            activity.triggerFadeOutThread();
        });
    }
}

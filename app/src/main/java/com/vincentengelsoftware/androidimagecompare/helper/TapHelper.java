package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.ImageView;

public class TapHelper {
    public static void setOnClickListener(ImageView imageViewListener, ImageView imageViewTarget)
    {
        imageViewListener.setOnClickListener(new View.OnClickListener() {
            private long lastClickTime = 0;
            @Override
            public void onClick(View view) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < 300) {
                    onDoubleClick();
                    lastClickTime = 0;
                }
                lastClickTime = clickTime;
            }

            private void onDoubleClick() {
                imageViewListener.setVisibility(View.INVISIBLE);
                imageViewTarget.setVisibility(View.VISIBLE);
            }
        });
    }
}

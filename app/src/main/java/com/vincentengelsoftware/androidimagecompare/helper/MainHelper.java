package com.vincentengelsoftware.androidimagecompare.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

public class MainHelper {
    @SuppressLint("ClickableViewAccessibility")
    public static void passClickToUnderlyingView(FrameLayout frameLayout, ImageButton imageButton)
    {
        frameLayout.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                int x = (int) event.getX();
                int y = (int) event.getY();

                int[] imageButtonLocation = new int[2];
                imageButton.getLocationOnScreen(imageButtonLocation);

                int[] viewLocation = new int[2];
                v.getLocationOnScreen(viewLocation);


                imageButtonLocation[0] = imageButtonLocation[0] - viewLocation[0];
                imageButtonLocation[1] = imageButtonLocation[1] - viewLocation[1];

                if (
                        x >= imageButtonLocation[0]
                                && x <= (imageButtonLocation[0] + imageButton.getWidth())
                                && y >= imageButtonLocation[1]
                                && y <= (imageButtonLocation[1] + imageButton.getHeight())
                ) {
                    imageButton.callOnClick();
                }
            }
            return true;
        });
    }

    public static void addSwapImageLogic(
            ImageButton imageButton,
            ImageHolder mutableUriOne,
            ImageHolder mutableUriTwo,
            ImageView imageViewOne,
            ImageView imageViewTwo
    ) {
        imageButton.setOnClickListener(view -> {
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.updateFromImageHolder(mutableUriOne);
            mutableUriOne.updateFromImageHolder(mutableUriTwo);
            mutableUriTwo.updateFromImageHolder(imageHolder);

            imageViewOne.setImageBitmap(mutableUriOne.bitmapSmall);
            imageViewTwo.setImageBitmap(mutableUriTwo.bitmapSmall);
        });
    }

    public static void requestPermission(final Activity context)
    {
        ActivityCompat.requestPermissions(
                context,
                new String[]{Manifest.permission.CAMERA},
                1
        );
    }

    public static boolean checkPermission(final Activity context)
    {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }
}

package com.vincentengelsoftware.androidimagecompare.helper;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.globals.Status;
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

    public static void addRotateImageLogic(
            ImageButton imageButton,
            ImageHolder imageHolder,
            ImageView imageView
    ) {
        imageButton.setOnClickListener(view -> {
            if (imageHolder.bitmap == null || Status.activityIsOpening) {
                return;
            }
            imageHolder.rotatePreviewImage();
            ImageUpdater.updateImageViewImage(
                    imageView,
                    imageHolder,
                    ImageUpdater.SMALL
            );
        });
    }

    public static void addSwapImageLogic(
            ImageButton imageButton,
            ImageHolder imageHolderOne,
            ImageHolder imageHolderTwo,
            ImageView imageViewOne,
            ImageView imageViewTwo,
            TextView imageTextViewNameLeft,
            TextView imageTextViewNameRight
    ) {
        imageButton.setOnClickListener(view -> {
            if (imageHolderOne.bitmap == null || imageHolderTwo.bitmap == null || Status.activityIsOpening) {
                return;
            }
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.updateFromImageHolder(imageHolderOne);
            imageHolderOne.updateFromImageHolder(imageHolderTwo);
            imageHolderTwo.updateFromImageHolder(imageHolder);

            ImageUpdater.updateImageViewImage(
                    imageViewOne,
                    imageHolderOne,
                    ImageUpdater.SMALL
            );
            ImageUpdater.updateImageViewImage(
                    imageViewTwo,
                    imageHolderTwo,
                    ImageUpdater.SMALL
            );

            imageTextViewNameLeft.setText(imageHolderOne.getImageName());
            imageTextViewNameRight.setText(imageHolderTwo.getImageName());
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

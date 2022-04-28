package com.vincentengelsoftware.androidimagecompare.helper;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableUri;

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
            UtilMutableUri mutableUriOne,
            UtilMutableUri mutableUriTwo,
            ImageView imageViewOne,
            ImageView imageViewTwo
    ) {
        imageButton.setOnClickListener(view -> {
            Uri temp = mutableUriOne.uri;
            mutableUriOne.uri = mutableUriTwo.uri;
            mutableUriTwo.uri = temp;

            imageViewOne.setImageURI(mutableUriOne.uri);

            imageViewTwo.setImageURI(mutableUriTwo.uri);
        });
    }
}

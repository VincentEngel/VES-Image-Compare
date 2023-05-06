package com.vincentengelsoftware.androidimagecompare.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.vincentengelsoftware.androidimagecompare.MainActivity;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.RequestPermissionCodes;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.util.ImageHolder;

public class MainHelper {
    public static void addRotateImageLogic(
            ImageButton imageButton,
            ImageHolder imageHolder,
            ImageView imageView
    ) {
        imageButton.setOnClickListener(view -> {
            if (imageHolder.getBitmap() == null || Status.activityIsOpening) {
                return;
            }
            imageHolder.rotatePreviewImage();
            imageHolder.updateImageViewPreviewImage(imageView);
        });
    }

    public static void addSwapImageLogic(
            ImageButton imageButton,
            ImageHolder imageHolderOne,
            ImageHolder imageHolderTwo,
            ImageView imageViewOne,
            ImageView imageViewTwo,
            TextView imageTextViewNameLeft,
            TextView imageTextViewNameRight,
            SwitchCompat resizeImageLeft,
            SwitchCompat resizeImageRight
    ) {
        // Just replace imageHolders <.<
        imageButton.setOnClickListener(view -> {
            if (imageHolderOne.getBitmap() == null || imageHolderTwo.getBitmap() == null || Status.activityIsOpening) {
                return;
            }
            ImageHolder imageHolder = new ImageHolder();
            imageHolder.updateFromImageHolder(imageHolderOne);
            imageHolderOne.updateFromImageHolder(imageHolderTwo);
            imageHolderTwo.updateFromImageHolder(imageHolder);

            imageHolderOne.updateImageViewPreviewImage(imageViewOne);
            imageHolderTwo.updateImageViewPreviewImage(imageViewTwo);

            imageTextViewNameLeft.setText(imageHolderOne.getImageName());
            imageTextViewNameRight.setText(imageHolderTwo.getImageName());

            resizeImageLeft.setChecked(imageHolderOne.isResizeImageToScreen());
            resizeImageRight.setChecked(imageHolderTwo.isResizeImageToScreen());

            // Take two camera pictures => swap them once
            // Take another camera picture
            // App crashes
            // App restores last state
            // Same Image on both sides => Wrong image was loaded because they have the same path
            String temp = MainActivity.leftImageUri;
            MainActivity.leftImageUri = MainActivity.rightImageUri;
            MainActivity.rightImageUri = temp;
        });
    }

    public static void requestPermission(final Activity context)
    {
        ActivityCompat.requestPermissions(
                context,
                new String[]{Manifest.permission.CAMERA},
                RequestPermissionCodes.CAMERA // TODO handle accepted in MainActivity to open camera automatically
        );
    }

    public static boolean checkPermission(final Activity context)
    {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static String getImageName(Context context, Uri uri) {
        try {
            DocumentFile df = DocumentFile.fromSingleUri(context, uri);
            if (df != null && df.getName() != null) {
                return df.getName();
            }
        } catch (Exception ignored) {
        }

        return Images.DEFAULT_IMAGE_NAME;
    }

    public static void updateImageFromIntent(
            ImageHolder imageHolder,
            Bitmap bitmap,
            int maxSideSize,
            int maxSideSizeForSmallBitmap,
            String imageName,
            ImageView imageView,
            TextView textView
    )
    {
        imageHolder.updateFromBitmap(
                bitmap,
                maxSideSize,
                maxSideSizeForSmallBitmap,
                imageName
        );
        imageHolder.updateImageViewPreviewImage(imageView);
        textView.setText(imageHolder.getImageName());
    }
}

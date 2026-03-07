package com.vincentengelsoftware.androidimagecompare.helper;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import com.vincentengelsoftware.androidimagecompare.Activities.MainActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.globals.RequestPermissionCodes;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.util.ImageInfoHolder;

public class MainHelper {
    public static void addRotateImageLogic(
            ImageButton imageButton,
            ImageInfoHolder imageInfoHolder,
            ImageView imageView
    ) {
        imageButton.setOnClickListener(view -> {
            if (imageInfoHolder.getBitmap() == null || Status.activityIsOpening) {
                return;
            }
            imageInfoHolder.rotatePreviewImage();
            imageInfoHolder.updateImageViewPreviewImage(imageView);
        });
    }

    public static void addSwapImageLogic(
            ImageButton imageButton,
            ImageInfoHolder imageInfoHolderOne,
            ImageInfoHolder imageInfoHolderTwo,
            ImageView imageViewOne,
            ImageView imageViewTwo,
            TextView imageTextViewNameLeft,
            TextView imageTextViewNameRight
    ) {
        // Just replace imageHolders <.<
        imageButton.setOnClickListener(view -> {
            if (imageInfoHolderOne.getBitmap() == null || imageInfoHolderTwo.getBitmap() == null || Status.activityIsOpening) {
                return;
            }
            ImageInfoHolder imageInfoHolder = new ImageInfoHolder();
            imageInfoHolder.updateFromImageHolder(imageInfoHolderOne);
            imageInfoHolderOne.updateFromImageHolder(imageInfoHolderTwo);
            imageInfoHolderTwo.updateFromImageHolder(imageInfoHolder);

            imageInfoHolderOne.updateImageViewPreviewImage(imageViewOne);
            imageInfoHolderTwo.updateImageViewPreviewImage(imageViewTwo);

            imageTextViewNameLeft.setText(imageInfoHolderOne.getImageName());
            imageTextViewNameRight.setText(imageInfoHolderTwo.getImageName());

            // Take two camera pictures => swap them once
            // Take another camera picture
            // App crashes
            // App restores last state
            // Same Image on both sides => Wrong image was loaded because they have the same path
            Uri temp = MainActivity.leftImageUri;
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
            if (df.getName() != null) {
                return df.getName();
            }

            ContentResolver cR = context.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            return mime.getExtensionFromMimeType(cR.getType(uri));
        } catch (Exception ignored) {
        }

        return context.getString(R.string.unknown);
    }

    public static void updateImageFromIntent(
            ImageInfoHolder imageInfoHolder,
            Bitmap bitmap,
            int maxSideSize,
            int maxSideSizeForSmallBitmap,
            String imageName,
            ImageView imageView,
            TextView textView
    )
    {
        imageInfoHolder.updateFromBitmap(
                bitmap,
                maxSideSize,
                maxSideSizeForSmallBitmap,
                imageName
        );
        imageInfoHolder.updateImageViewPreviewImage(imageView);
        textView.setText(imageInfoHolder.getImageName());
    }
}

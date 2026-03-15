package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.RequestPermissionCodes;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainHelper {
  public static void addRotateImageLogic(
      ImageButton imageButton,
      ImageInfoHolder imageInfoHolder,
      ImageView imageView,
      AtomicBoolean openingActivity) {
    imageButton.setOnClickListener(
        view -> {
          if (imageInfoHolder.getBitmap() == null || openingActivity.get()) {
            return;
          }
          imageInfoHolder.rotatePreviewImage();
          imageInfoHolder.updateImageViewPreviewImage(imageView);
        });
  }

  public static void addMirrorImageLogic(
      ImageButton imageButton,
      ImageInfoHolder imageInfoHolder,
      ImageView imageView,
      AtomicBoolean openingActivity) {
    imageButton.setOnClickListener(
        view -> {
          if (imageInfoHolder.getBitmap() == null || openingActivity.get()) {
            return;
          }
          imageInfoHolder.mirrorPreviewImage();
          imageInfoHolder.updateImageViewPreviewImage(imageView);
        });
  }

  public static void addSwapImageLogic(
      ImageButton imageButton,
      ImageView imageViewOne,
      ImageView imageViewTwo,
      TextView imageTextViewNameLeft,
      TextView imageTextViewNameRight,
      AtomicBoolean openingActivity,
      ImageSessionState imageSessionState) {
    imageButton.setOnClickListener(
        view -> {
          if (imageSessionState.getFirstImageInfoHolder().getBitmap() == null
              || imageSessionState.getSecondImageInfoHolder().getBitmap() == null
              || openingActivity.get()) {
            return;
          }

          imageSessionState.swap();

          imageTextViewNameLeft.setText(imageSessionState.getFirstImageInfoHolder().getImageName());
          imageTextViewNameRight.setText(
              imageSessionState.getSecondImageInfoHolder().getImageName());

          imageSessionState.getFirstImageInfoHolder().updateImageViewPreviewImage(imageViewOne);
          imageSessionState.getSecondImageInfoHolder().updateImageViewPreviewImage(imageViewTwo);
        });
  }

  public static void requestPermission(final Activity context) {
    ActivityCompat.requestPermissions(
        context,
        new String[] {Manifest.permission.CAMERA},
        RequestPermissionCodes
            .CAMERA // TODO handle accepted in MainActivity to open camera automatically
        );
  }

  public static boolean checkPermission(final Activity context) {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED;
  }

  public static String getImageName(Context context, Uri uri) {
    try {
      DocumentFile df = DocumentFile.fromSingleUri(context, uri);
      if (df.getName() != null) {
        return df.getName();
      }

      return uri.getLastPathSegment();
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
      TextView textView) {
    imageInfoHolder.updateFromBitmap(bitmap, maxSideSize, maxSideSizeForSmallBitmap, imageName);
    imageInfoHolder.updateImageViewPreviewImage(imageView);
    textView.setText(imageInfoHolder.getImageName());
  }
}

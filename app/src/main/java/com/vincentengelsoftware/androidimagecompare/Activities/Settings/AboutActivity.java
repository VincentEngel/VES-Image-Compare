package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityAboutBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.TextViewModifier;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Status.activityIsOpening = false;

        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkGithubProject);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkLibrarySubsamplingScaleImageView);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkLibraryMetadataExtractor);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkColorTheme);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkGoogleMaterialIcons);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkDittoPhotoComparer);

        binding.infoBtnOpenPlaystore.setOnClickListener(view -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
            } catch (ActivityNotFoundException e1) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });

        String version = getString(R.string.unknown);
        try {
            PackageInfo pinfo = getPackageInfo();
            version = "v" + pinfo.versionName;
        } catch (Exception ignored) {
        }

        binding.infoVersion.setText(version);
    }

    @SuppressWarnings("deprecation")
    private PackageInfo getPackageInfo() throws PackageManager.NameNotFoundException {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return getPackageManager().getPackageInfo(getPackageName(), PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA));
        }

        return getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
    }
}
package com.vincentengelsoftware.androidimagecompare.activities.settings;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityAboutBinding;
import com.vincentengelsoftware.androidimagecompare.helper.PackageInfoHelper;
import com.vincentengelsoftware.androidimagecompare.helper.PlayStoreNavigator;
import com.vincentengelsoftware.androidimagecompare.helper.TextViewModifier;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityAboutBinding binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkGithubProject);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkLibrarySubsamplingScaleImageView);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkColorTheme);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkGoogleMaterialIcons);
        TextViewModifier.makeLinkClickable(binding.infoTextViewLinkDittoPhotoComparer);

        binding.infoBtnOpenPlaystore.setOnClickListener(view -> PlayStoreNavigator.openPlayStoreAppPage(this));

        String version = getString(R.string.unknown);
        try {
            version = "v" + PackageInfoHelper.getPackageVersion(this);
        } catch (Exception ignored) {
        }

        binding.infoVersion.setText(version);
    }
}
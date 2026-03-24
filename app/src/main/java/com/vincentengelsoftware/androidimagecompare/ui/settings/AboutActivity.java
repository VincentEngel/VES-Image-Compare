package com.vincentengelsoftware.androidimagecompare.ui.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityAboutBinding;
import com.vincentengelsoftware.androidimagecompare.ui.util.PlayStoreNavigator;
import com.vincentengelsoftware.androidimagecompare.ui.util.TextViewModifier;
import com.vincentengelsoftware.androidimagecompare.util.AppVersion;

/** Displays app information: version, open-source acknowledgements, and a Play Store link. */
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

    binding.infoBtnOpenPlaystore.setOnClickListener(
        view -> PlayStoreNavigator.openPlayStoreAppPage(this));

    binding.infoVersion.setText(AppVersion.getVersionName(this));
  }
}

package com.vincentengelsoftware.androidimagecompare.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityConfigsBinding;
import com.vincentengelsoftware.androidimagecompare.ui.settings.usersettings.UserSettingsActivity;
import com.vincentengelsoftware.androidimagecompare.ui.util.PlayStoreNavigator;
import com.vincentengelsoftware.androidimagecompare.util.AppVersion;

/**
 * Entry-point screen for the app's configuration section.
 *
 * <p>Displays the current version and provides navigation to the Privacy Policy, About, and User
 * Settings screens, as well as the Play Store listing.
 */
public class ConfigActivity extends AppCompatActivity {

  private ActivityConfigsBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = ActivityConfigsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setUpViews();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  private void setUpViews() {
    binding.settingsVersion.setText(AppVersion.getVersionName(this));

    binding.configsActionPrivacyPolicy.setOnClickListener(
        view -> startActivity(new Intent(this, PrivacyPolicyActivity.class)));

    binding.configsActionAppInfo.setOnClickListener(
        view -> startActivity(new Intent(this, AboutActivity.class)));

    binding.configsActionSettings.setOnClickListener(
        view -> startActivity(new Intent(this, UserSettingsActivity.class)));

    binding.configsOpenPlaystore.setOnClickListener(
        view -> PlayStoreNavigator.openPlayStoreAppPage(this));
  }
}

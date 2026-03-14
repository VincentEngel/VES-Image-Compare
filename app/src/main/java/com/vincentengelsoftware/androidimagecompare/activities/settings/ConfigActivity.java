package com.vincentengelsoftware.androidimagecompare.activities.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.vincentengelsoftware.androidimagecompare.activities.settings.userSettings.UserSettingsActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityConfigsBinding;
import com.vincentengelsoftware.androidimagecompare.helper.AppVersion;
import com.vincentengelsoftware.androidimagecompare.helper.PlayStoreNavigator;

public class ConfigActivity extends AppCompatActivity {

    private ActivityConfigsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityConfigsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setUpViews();
    }

    private void setUpViews() {
        binding.settingsVersion.setText(AppVersion.getVersionName(this));

        binding.configsActionPrivacyPolicy.setOnClickListener(view ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));

        binding.configsActionAppInfo.setOnClickListener(view ->
                startActivity(new Intent(this, AboutActivity.class)));

        binding.configsActionSettings.setOnClickListener(view ->
                startActivity(new Intent(this, UserSettingsActivity.class)));

        binding.configsOpenPlaystore.setOnClickListener(view ->
                PlayStoreNavigator.openPlayStoreAppPage(this));
    }
}
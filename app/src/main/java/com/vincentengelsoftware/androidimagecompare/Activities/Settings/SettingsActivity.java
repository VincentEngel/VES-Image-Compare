package com.vincentengelsoftware.androidimagecompare.Activities.Settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivitySettingsBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.services.Settings.UserSettings;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {
    private UserSettings userSettings;
    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.userSettings = UserSettings.getInstance(new KeyValueStorage(getApplicationContext()));
        Theme.updateTheme(userSettings.getTheme());
        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Status.activityIsOpening = false;

        this.setUp();
        this.applyCurrentSettings();
    }

    private void setUp() {
        this.setUpThemeToggleButton(binding.homeTheme);

        binding.settingsSave.setOnClickListener(view -> {
            int maxZoomValue = Integer.parseInt(Objects.requireNonNull(binding.settingsMaxZoom.getText()).toString());
            if (maxZoomValue < 1) {
                maxZoomValue = 1;
                Toast.makeText(getApplicationContext(), getString(R.string.error_msg_invalid_input_number_gt_zero), Toast.LENGTH_LONG).show();
            }
            this.userSettings.setMaxZoom(maxZoomValue);

            float minZoomValue = Float.parseFloat(Objects.requireNonNull(binding.settingsMinZoom.getText()).toString());
            if (minZoomValue <= 0F) {
                minZoomValue = 0.1F;
                Toast.makeText(getApplicationContext(), getString(R.string.error_msg_invalid_input_number_gt_zero), Toast.LENGTH_LONG).show();
            }
            this.userSettings.setMinZoom(minZoomValue);

            this.applyCurrentSettings();
            Toast.makeText(getApplicationContext(), getString(R.string.settings_save_success), Toast.LENGTH_LONG).show();
        });

        binding.settingsSwitchResetZoomOnLinking.setOnClickListener(view -> {
            this.userSettings.setResetImageOnLinking(binding.settingsSwitchResetZoomOnLinking.isChecked());
        });

        binding.settingsMirroringNatural.setOnClickListener(view -> {
            this.userSettings.setMirroringType(Status.NATURAL_MIRRORING);
            binding.settingsMirroringExplanation.setText(R.string.settings_mirroring_natural_description);
        });

        binding.settingsMirroringStrict.setOnClickListener(view -> {
            this.userSettings.setMirroringType(Status.STRICT_MIRRORING);
            binding.settingsMirroringExplanation.setText(R.string.settings_mirroring_strict_description);
        });

        binding.settingsMirroringLoose.setOnClickListener(view -> {
            this.userSettings.setMirroringType(Status.LOOSE_MIRRORING);
            binding.settingsMirroringExplanation.setText(R.string.settings_mirroring_loose_description);
        });

        binding.settingsTapHideModeBtnInvisible.setOnClickListener(view -> {
            this.userSettings.setTypHideMode(Status.TAP_HIDE_MODE_INVISIBLE);
            binding.settingsTapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_invisible);
        });

        binding.settingsTapHideModeBtnBackground.setOnClickListener(view -> {
            this.userSettings.setTypHideMode(Status.TAP_HIDE_MODE_BACKGROUND);
            binding.settingsTapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_background);
        });

        binding.settingsReset.setOnClickListener(view -> {
            this.userSettings.resetAllSettings();
            this.applyCurrentSettings();
            Toast.makeText(getApplicationContext(), getString(R.string.settings_reset_success), Toast.LENGTH_LONG).show();
            recreate();
        });
    }

    private void applyCurrentSettings() {
        binding.settingsMaxZoom.setText(String.valueOf(this.userSettings.getMaxZoom()));
        binding.settingsMinZoom.setText(String.valueOf(this.userSettings.getMinZoom()));

        Theme.updateButtonText(binding.homeTheme, this.userSettings.getTheme());
        binding.settingsSwitchResetZoomOnLinking.setChecked(this.userSettings.getResetImageOnLink());

        this.applyMirroringSettings();
        this.applyTapModeSettings();
    }

    private void applyMirroringSettings() {
        if (this.userSettings.getMirroringType() == Status.NATURAL_MIRRORING) {
            binding.settingsMirroringNatural.setChecked(true);
            binding.settingsMirroringStrict.setChecked(false);
            binding.settingsMirroringLoose.setChecked(false);
            binding.settingsMirroringExplanation.setText(R.string.settings_mirroring_natural_description);
        } else if (this.userSettings.getMirroringType() == Status.STRICT_MIRRORING) {
            binding.settingsMirroringNatural.setChecked(false);
            binding.settingsMirroringStrict.setChecked(true);
            binding.settingsMirroringLoose.setChecked(false);
            binding.settingsMirroringExplanation.setText(R.string.settings_mirroring_strict_description);
        } else if (this.userSettings.getMirroringType() == Status.LOOSE_MIRRORING) {
            binding.settingsMirroringNatural.setChecked(false);
            binding.settingsMirroringStrict.setChecked(false);
            binding.settingsMirroringLoose.setChecked(true);
            binding.settingsMirroringExplanation.setText(R.string.settings_mirroring_loose_description);
        }
    }

    private void applyTapModeSettings() {
        if (this.userSettings.getTapHideMode() == Status.TAP_HIDE_MODE_INVISIBLE) {
            binding.settingsTapHideModeBtnInvisible.setChecked(true);
            binding.settingsTapHideModeBtnBackground.setChecked(false);
            binding.settingsTapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_invisible);
        } else if (this.userSettings.getTapHideMode() == Status.TAP_HIDE_MODE_BACKGROUND) {
            binding.settingsTapHideModeBtnInvisible.setChecked(false);
            binding.settingsTapHideModeBtnBackground.setChecked(true);
            binding.settingsTapHideModeDescription.setText(R.string.settings_tap_hide_mode_description_background);
        }
    }

    private void setUpThemeToggleButton(Button buttonTheme) {
        buttonTheme.setOnClickListener(view -> {
            this.userSettings.setTheme((this.userSettings.getTheme() + 1) % 3);
            Theme.updateButtonText(buttonTheme, this.userSettings.getTheme());
            Theme.updateTheme(this.userSettings.getTheme());
        });
    }
}
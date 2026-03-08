package com.vincentengelsoftware.androidimagecompare.Activities.Settings.UserSettings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityUserSettingsBinding;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.Theme;
import com.vincentengelsoftware.androidimagecompare.services.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.services.Settings.UserSettings;

import java.util.Objects;

public class UserSettingsActivity extends AppCompatActivity {
    private UserSettingsPresenter presenter;
    private ActivityUserSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserSettings userSettings = UserSettings.getInstance(new KeyValueStorage(getApplicationContext()));
        Theme.updateTheme(userSettings.getTheme());
        super.onCreate(savedInstanceState);
        this.presenter = new UserSettingsPresenter(userSettings);

        binding = ActivityUserSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        this.setUp();
        this.render(presenter.buildUiState());
    }

    private void setUp() {
        this.setUpThemeToggleButton(binding.homeTheme);

        binding.settingsSave.setOnClickListener(view -> {
            try {
                int maxZoomValue = Integer.parseInt(
                        Objects.requireNonNull(binding.settingsMaxZoom.getText()).toString());
                float minZoomValue = Float.parseFloat(
                        Objects.requireNonNull(binding.settingsMinZoom.getText()).toString());

                UserSettingsPresenter.SaveZoomResult result = presenter.saveZoom(maxZoomValue, minZoomValue);
                this.render(presenter.buildUiState());
                if (result.hadInvalidInput()) {
                    Toast.makeText(this, getString(R.string.error_msg_invalid_input_number_gt_zero), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, getString(R.string.settings_save_success), Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.error_msg_invalid_input_number_gt_zero), Toast.LENGTH_LONG).show();
            }
        });

        binding.settingsSwitchResetZoomOnLinking.setOnClickListener(view ->
                presenter.setResetImageOnLinking(binding.settingsSwitchResetZoomOnLinking.isChecked()));

        binding.settingsMirroringNatural.setOnClickListener(view -> {
            presenter.setMirroringType(Status.NATURAL_MIRRORING);
            binding.settingsMirroringExplanation.setText(UserSettingsPresenter.mirroringExplanationResId(Status.NATURAL_MIRRORING));
        });

        binding.settingsMirroringStrict.setOnClickListener(view -> {
            presenter.setMirroringType(Status.STRICT_MIRRORING);
            binding.settingsMirroringExplanation.setText(UserSettingsPresenter.mirroringExplanationResId(Status.STRICT_MIRRORING));
        });

        binding.settingsMirroringLoose.setOnClickListener(view -> {
            presenter.setMirroringType(Status.LOOSE_MIRRORING);
            binding.settingsMirroringExplanation.setText(UserSettingsPresenter.mirroringExplanationResId(Status.LOOSE_MIRRORING));
        });

        binding.settingsTapHideModeBtnInvisible.setOnClickListener(view -> {
            presenter.setTapHideMode(Status.TAP_HIDE_MODE_INVISIBLE);
            binding.settingsTapHideModeDescription.setText(UserSettingsPresenter.tapHideModeDescriptionResId(Status.TAP_HIDE_MODE_INVISIBLE));
        });

        binding.settingsTapHideModeBtnBackground.setOnClickListener(view -> {
            presenter.setTapHideMode(Status.TAP_HIDE_MODE_BACKGROUND);
            binding.settingsTapHideModeDescription.setText(UserSettingsPresenter.tapHideModeDescriptionResId(Status.TAP_HIDE_MODE_BACKGROUND));
        });

        binding.settingsReset.setOnClickListener(view -> {
            this.render(presenter.resetAllSettings());
            Toast.makeText(this, getString(R.string.settings_reset_success), Toast.LENGTH_LONG).show();
            recreate();
        });
    }

    private void render(UserSettingsUiState state) {
        binding.settingsMaxZoom.setText(state.maxZoom());
        binding.settingsMinZoom.setText(state.minZoom());

        binding.homeTheme.setText(state.themeButtonTextResId());
        binding.settingsSwitchResetZoomOnLinking.setChecked(state.resetImageOnLinking());

        binding.settingsMirroringNatural.setChecked(state.mirroringNaturalChecked());
        binding.settingsMirroringStrict.setChecked(state.mirroringStrictChecked());
        binding.settingsMirroringLoose.setChecked(state.mirroringLooseChecked());
        binding.settingsMirroringExplanation.setText(state.mirroringExplanationResId());

        binding.settingsTapHideModeBtnInvisible.setChecked(state.tapHideModeInvisibleChecked());
        binding.settingsTapHideModeBtnBackground.setChecked(state.tapHideModeBackgroundChecked());
        binding.settingsTapHideModeDescription.setText(state.tapHideModeDescriptionResId());
    }

    private void setUpThemeToggleButton(Button buttonTheme) {
        buttonTheme.setOnClickListener(view -> {
            int newTheme = presenter.cycleTheme();
            Theme.updateTheme(newTheme);
            buttonTheme.setText(UserSettingsPresenter.themeButtonTextResId(newTheme));
        });
    }
}
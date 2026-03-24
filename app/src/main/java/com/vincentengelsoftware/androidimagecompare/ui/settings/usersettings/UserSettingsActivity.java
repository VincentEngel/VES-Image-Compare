package com.vincentengelsoftware.androidimagecompare.ui.settings.usersettings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.data.preferences.KeyValueStorage;
import com.vincentengelsoftware.androidimagecompare.data.preferences.UserSettings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityUserSettingsBinding;
import com.vincentengelsoftware.androidimagecompare.ui.util.Theme;

/**
 * Screen that lets the user view and modify all configurable app preferences.
 *
 * <p>Delegates all business logic to {@link UserSettingsPresenter}. The Activity is responsible
 * only for inflating views, wiring click listeners, and calling {@link
 * #render(UserSettingsUiState)} to reflect the current state.
 */
public class UserSettingsActivity extends AppCompatActivity {

  private UserSettingsPresenter presenter;
  private ActivityUserSettingsBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UserSettings userSettings =
        UserSettings.getInstance(new KeyValueStorage(getApplicationContext()));
    Theme.updateTheme(userSettings.getTheme());
    super.onCreate(savedInstanceState);
    presenter = new UserSettingsPresenter(userSettings);

    binding = ActivityUserSettingsBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());

    setUp();
    render(presenter.buildUiState());
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    binding = null;
  }

  // ── Setup ─────────────────────────────────────────────────────────────────

  private void setUp() {
    setUpThemeToggleButton(binding.homeTheme);
    setUpSaveButton();
    setUpSwitches();
    setUpMirroringButtons();
    setUpTapHideModeButtons();
    setUpDiffCircleColorButtons();
    setUpResetButton();
  }

  private void setUpSaveButton() {
    binding.settingsSave.setOnClickListener(
        view -> {
          try {
            int maxZoomValue = Integer.parseInt(String.valueOf(binding.settingsMaxZoom.getText()));
            float minZoomValue =
                Float.parseFloat(String.valueOf(binding.settingsMinZoom.getText()));
            int maxDifferencesValue =
                Integer.parseInt(String.valueOf(binding.settingsMaxDifferences.getText()));

            UserSettingsPresenter.SaveZoomResult zoomResult =
                presenter.saveZoom(maxZoomValue, minZoomValue);
            boolean diffError = presenter.saveDifferencesMaxCount(maxDifferencesValue);
            render(presenter.buildUiState());

            if (zoomResult.hadInvalidInput() || diffError) {
              Toast.makeText(
                      this,
                      getString(R.string.error_msg_invalid_input_number_gt_zero),
                      Toast.LENGTH_LONG)
                  .show();
            } else {
              Toast.makeText(this, getString(R.string.settings_save_success), Toast.LENGTH_LONG)
                  .show();
            }
          } catch (NumberFormatException e) {
            Toast.makeText(
                    this,
                    getString(R.string.error_msg_invalid_input_not_a_number),
                    Toast.LENGTH_LONG)
                .show();
          }
        });
  }

  private void setUpSwitches() {
    binding.settingsSwitchResetZoomOnLinking.setOnClickListener(
        view ->
            presenter.setResetImageOnLinking(binding.settingsSwitchResetZoomOnLinking.isChecked()));

    binding.settingsSwitchFullscreen.setOnClickListener(
        view -> presenter.setShowNavigationBar(binding.settingsSwitchFullscreen.isChecked()));
  }

  private void setUpMirroringButtons() {
    binding.settingsMirroringNatural.setOnClickListener(
        view -> {
          presenter.setMirroringType(Status.NATURAL_MIRRORING);
          binding.settingsMirroringExplanation.setText(
              UserSettingsPresenter.mirroringExplanationResId(Status.NATURAL_MIRRORING));
        });

    binding.settingsMirroringStrict.setOnClickListener(
        view -> {
          presenter.setMirroringType(Status.STRICT_MIRRORING);
          binding.settingsMirroringExplanation.setText(
              UserSettingsPresenter.mirroringExplanationResId(Status.STRICT_MIRRORING));
        });

    binding.settingsMirroringLoose.setOnClickListener(
        view -> {
          presenter.setMirroringType(Status.LOOSE_MIRRORING);
          binding.settingsMirroringExplanation.setText(
              UserSettingsPresenter.mirroringExplanationResId(Status.LOOSE_MIRRORING));
        });
  }

  private void setUpTapHideModeButtons() {
    binding.settingsTapHideModeBtnInvisible.setOnClickListener(
        view -> {
          presenter.setTapHideMode(Status.TAP_HIDE_MODE_INVISIBLE);
          binding.settingsTapHideModeDescription.setText(
              UserSettingsPresenter.tapHideModeDescriptionResId(Status.TAP_HIDE_MODE_INVISIBLE));
        });

    binding.settingsTapHideModeBtnBackground.setOnClickListener(
        view -> {
          presenter.setTapHideMode(Status.TAP_HIDE_MODE_BACKGROUND);
          binding.settingsTapHideModeDescription.setText(
              UserSettingsPresenter.tapHideModeDescriptionResId(Status.TAP_HIDE_MODE_BACKGROUND));
        });
  }

  private void setUpDiffCircleColorButtons() {
    binding.settingsDiffCircleColorRed.setOnClickListener(
        view -> presenter.setDifferencesCircleColor(Status.DIFF_CIRCLE_COLOR_RED));

    binding.settingsDiffCircleColorBlue.setOnClickListener(
        view -> presenter.setDifferencesCircleColor(Status.DIFF_CIRCLE_COLOR_BLUE));

    binding.settingsDiffCircleColorGreen.setOnClickListener(
        view -> presenter.setDifferencesCircleColor(Status.DIFF_CIRCLE_COLOR_GREEN));
  }

  private void setUpResetButton() {
    binding.settingsReset.setOnClickListener(
        view -> {
          render(presenter.resetAllSettings());
          Toast.makeText(this, getString(R.string.settings_reset_success), Toast.LENGTH_LONG)
              .show();
          recreate();
        });
  }

  private void setUpThemeToggleButton(Button buttonTheme) {
    buttonTheme.setOnClickListener(
        view -> {
          int newTheme = presenter.cycleTheme();
          Theme.updateTheme(newTheme);
          buttonTheme.setText(UserSettingsPresenter.themeButtonTextResId(newTheme));
        });
  }

  // ── Render ────────────────────────────────────────────────────────────────

  private void render(UserSettingsUiState state) {
    binding.settingsMaxZoom.setText(state.maxZoom());
    binding.settingsMinZoom.setText(state.minZoom());

    binding.homeTheme.setText(state.themeButtonTextResId());
    binding.settingsSwitchResetZoomOnLinking.setChecked(state.resetImageOnLinking());
    binding.settingsSwitchFullscreen.setChecked(state.fullscreen());

    binding.settingsMirroringNatural.setChecked(state.mirroringNaturalChecked());
    binding.settingsMirroringStrict.setChecked(state.mirroringStrictChecked());
    binding.settingsMirroringLoose.setChecked(state.mirroringLooseChecked());
    binding.settingsMirroringExplanation.setText(state.mirroringExplanationResId());

    binding.settingsTapHideModeBtnInvisible.setChecked(state.tapHideModeInvisibleChecked());
    binding.settingsTapHideModeBtnBackground.setChecked(state.tapHideModeBackgroundChecked());
    binding.settingsTapHideModeDescription.setText(state.tapHideModeDescriptionResId());

    binding.settingsMaxDifferences.setText(state.differencesMaxCount());
    binding.settingsDiffCircleColorRed.setChecked(state.differencesCircleColorRed());
    binding.settingsDiffCircleColorBlue.setChecked(state.differencesCircleColorBlue());
    binding.settingsDiffCircleColorGreen.setChecked(state.differencesCircleColorGreen());
  }
}

package com.vincentengelsoftware.androidimagecompare.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.constants.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.constants.Status;
import com.vincentengelsoftware.androidimagecompare.data.cache.CacheManager;
import com.vincentengelsoftware.androidimagecompare.data.preferences.UserSettings;
import com.vincentengelsoftware.androidimagecompare.databinding.ActivityMainBinding;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageInfoHolder;
import com.vincentengelsoftware.androidimagecompare.domain.model.ImageSessionState;
import com.vincentengelsoftware.androidimagecompare.ui.compare.CompareModeNames;
import com.vincentengelsoftware.androidimagecompare.util.ImageFileSaver;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Orchestrates the two-image processing pipeline and starts the chosen compare activity.
 *
 * <p>Both images are encoded to their compare output files in parallel on a dedicated two-thread
 * pool. The Activity is launched only after both files are ready. The calling thread is never
 * blocked; a lightweight wait-thread is spawned instead so the UI remains responsive and the
 * progress bar can animate while encoding is in progress.
 *
 * <p>Create one instance per {@code Activity} lifecycle and call {@link #shutdown()} from {@code
 * Activity.onDestroy} to release the thread pool.
 */
public class CompareActivityLauncher {

  private final ExecutorService executor;
  private final AtomicBoolean openingActivity;

  public CompareActivityLauncher(AtomicBoolean openingActivity) {
    this.executor = Executors.newFixedThreadPool(2);
    this.openingActivity = openingActivity;
  }

  /** Releases the internal thread pool. Call from {@code Activity.onDestroy}. */
  public void shutdown() {
    executor.shutdownNow();
  }

  /**
   * Validates preconditions, kicks off parallel image encoding, and starts {@code targetActivity}
   * once both output files are ready.
   *
   * @param activity the host activity (context + {@code startActivity})
   * @param targetActivity the compare-mode activity class to launch
   * @param userSettings source of UI preferences (extensions, sync, last mode)
   * @param sessionState owns the two {@link ImageInfoHolder}s
   * @param binding used to show/hide the progress bar and update the mode button
   */
  public void launch(
      Activity activity,
      Class<?> targetActivity,
      UserSettings userSettings,
      ImageSessionState sessionState,
      ActivityMainBinding binding) {
    // ── guard: only one launch at a time ─────────────────────────────────
    if (openingActivity.get()) {
      Toast.makeText(activity, R.string.error_message_general, Toast.LENGTH_SHORT).show();
      return;
    }
    openingActivity.set(true);

    ImageInfoHolder first = sessionState.getFirstImageInfoHolder();
    ImageInfoHolder second = sessionState.getSecondImageInfoHolder();

    if (first.getBitmap() == null || second.getBitmap() == null) {
      Toast.makeText(activity, R.string.error_msg_missing_images, Toast.LENGTH_SHORT).show();
      openingActivity.set(false);
      return;
    }

    // ── update the last-used mode button (on UI thread) ──────────────────
    String internalModeName = CompareModeNames.getInternalCompareModeNameByActivity(targetActivity);
    userSettings.setLastCompareMode(internalModeName);
    binding.mainButtonLastCompare.setText(
        CompareModeNames.getUserCompareModeNameFromInternalName(
            activity.getBaseContext(), internalModeName));

    // ── build the intent (UI thread) ─────────────────────────────────────
    Intent intent = new Intent(activity, targetActivity);
    intent.putExtra(IntentExtras.SHOW_EXTENSIONS, userSettings.isShowExtensions());
    intent.putExtra(IntentExtras.SYNC_IMAGE_INTERACTIONS, userSettings.isSyncImageInteractions());
    intent.putExtra(IntentExtras.HAS_HARDWARE_KEY, Status.HAS_HARDWARE_KEY);

    binding.pbProgess.setVisibility(View.VISIBLE);

    // ── submit both encode tasks in parallel ─────────────────────────────
    File cacheDir = activity.getCacheDir();
    File compareFileOne = CacheManager.getCompareFileOne(cacheDir);
    File compareFileTwo = CacheManager.getCompareFileTwo(cacheDir);

    boolean firstNeedsProcessing = first.requiresRecalculation(compareFileOne);
    boolean secondNeedsProcessing = second.requiresRecalculation(compareFileTwo);

    Future<Uri> futureOne =
        executor.submit(
            () -> {
              if (!firstNeedsProcessing) return Uri.fromFile(compareFileOne);
              Uri uri = ImageFileSaver.saveBitmapToFile(first.getAdjustedBitmap(), compareFileOne);
              if (uri != null) first.markSaved();
              return uri;
            });

    Future<Uri> futureTwo =
        executor.submit(
            () -> {
              if (!secondNeedsProcessing) return Uri.fromFile(compareFileTwo);
              Uri uri = ImageFileSaver.saveBitmapToFile(second.getAdjustedBitmap(), compareFileTwo);
              if (uri != null) second.markSaved();
              return uri;
            });

    // ── wait for results on a lightweight background thread ───────────────
    // This keeps the UI thread free so the progress bar can animate.
    new Thread(
            () -> {
              try {
                Uri uriOne = futureOne.get();
                Uri uriTwo = futureTwo.get();

                if (uriOne == null || uriTwo == null) {
                  throw new Exception("Error saving compare images");
                }

                intent.putExtra(IntentExtras.IMAGE_URI_ONE, uriOne.toString());
                intent.putExtra(IntentExtras.IMAGE_URI_TWO, uriTwo.toString());
                intent.putExtra(IntentExtras.IMAGE_NAME_ONE, first.getImageName());
                intent.putExtra(IntentExtras.IMAGE_NAME_TWO, second.getImageName());

                openingActivity.set(false);
                activity.startActivity(intent);
              } catch (Exception e) {
                openingActivity.set(false);
                activity.runOnUiThread(
                    () ->
                        Toast.makeText(activity, R.string.error_message_general, Toast.LENGTH_SHORT)
                            .show());
              } finally {
                activity.runOnUiThread(() -> binding.pbProgess.setVisibility(View.GONE));
              }
            },
            "CompareActivityLauncher-wait")
        .start();
  }
}

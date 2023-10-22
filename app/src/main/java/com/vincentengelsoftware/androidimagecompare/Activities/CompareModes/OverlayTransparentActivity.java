package com.vincentengelsoftware.androidimagecompare.Activities.CompareModes;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vincentengelsoftware.androidimagecompare.Activities.IntentExtras;
import com.vincentengelsoftware.androidimagecompare.R;
import com.vincentengelsoftware.androidimagecompare.animations.FadeActivity;
import com.vincentengelsoftware.androidimagecompare.animations.ResizeAnimation;
import com.vincentengelsoftware.androidimagecompare.globals.Images;
import com.vincentengelsoftware.androidimagecompare.globals.Status;
import com.vincentengelsoftware.androidimagecompare.helper.Calculator;
import com.vincentengelsoftware.androidimagecompare.helper.FullScreenHelper;
import com.vincentengelsoftware.androidimagecompare.helper.SyncZoom;
import com.vincentengelsoftware.androidimagecompare.helper.TransparentHelper;
import com.vincentengelsoftware.androidimagecompare.util.UtilMutableBoolean;
import com.vincentengelsoftware.androidimagecompare.ImageView.VesImageInterface;

public class OverlayTransparentActivity extends AppCompatActivity implements FadeActivity {
    public static UtilMutableBoolean sync = new UtilMutableBoolean();

    private final static UtilMutableBoolean continueHiding = new UtilMutableBoolean();
    private static Thread fadeOutThread;
    private static Thread fadeInThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
            fadeOutThread = null;
        }

        if (fadeInThread != null) {
            fadeInThread.interrupt();
            fadeInThread = null;
        }

        if (Status.activityIsOpening) {
            sync.value = getIntent().getBooleanExtra(IntentExtras.SYNCED_ZOOM, true);
        }

        Status.activityIsOpening = false;

        FullScreenHelper.setFullScreenFlags(this.getWindow());

        setContentView(R.layout.activity_overlay_transparent);

        VesImageInterface base = findViewById(R.id.overlay_transparent_image_view_base);
        base.addFadeListener(this);
        Images.first.updateVesImageViewWithAdjustedImage(base);

        VesImageInterface image_transparent = findViewById(R.id.overlay_transparent_image_view_transparent);
        image_transparent.addFadeListener(this);

        Images.second.updateVesImageViewWithAdjustedImage(image_transparent);

        image_transparent.bringToFront();


        ImageButton hideShow = findViewById(R.id.overlay_transparent_button_hide_front_image);

        SeekBar seekBar = findViewById(R.id.overlay_slide_seek_bar);

        TransparentHelper.makeTargetTransparent(seekBar, image_transparent, hideShow, this);

        seekBar.setProgress(50);

        hideShow.setOnClickListener(view -> {
            instantFadeIn();
            if (image_transparent.getVisibility() == View.VISIBLE) {
                hideShow.setImageResource(R.drawable.ic_visibility_off);
                image_transparent.setVisibility(View.GONE);
            } else if (seekBar.getProgress() <= 2) {
                seekBar.setProgress(3);
            } else {
                hideShow.setImageResource(R.drawable.ic_visibility);
                image_transparent.setVisibility(View.VISIBLE);
            }
            triggerFadeOutThread();
        });

        SyncZoom.setLinkedTargets(
                base,
                image_transparent,
                OverlayTransparentActivity.sync
        );
        SyncZoom.setUpSyncZoomToggleButton(
                base,
                image_transparent,
                findViewById(R.id.overlay_transparent_button_zoom_sync),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link),
                ContextCompat.getDrawable(getBaseContext(), R.drawable.ic_link_off),
                OverlayTransparentActivity.sync,
                this
        );

        if (getIntent().getBooleanExtra(IntentExtras.HAS_HARDWARE_KEY, false)) {
            LinearLayout linearLayout = findViewById(R.id.overlay_transparent_extensions);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) linearLayout.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            linearLayout.setLayoutParams(layoutParams);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        triggerFadeOutThread();
    }

    public void triggerFadeIn()
    {
        continueHiding.value = false;
        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
        }

        if (fadeInThread != null) {
            return;
        }

        fadeInThread = new Thread(() -> {
            runOnUiThread(() -> {
                try {
                    LinearLayout linearLayout = findViewById(R.id.overlay_transparent_extensions);

                    ResizeAnimation anim = new ResizeAnimation(
                            linearLayout,
                            Calculator.DpToPx2(48, getResources()),
                            ResizeAnimation.CHANGE_HEIGHT,
                            ResizeAnimation.IS_SHOWING_ANIMATION,
                            continueHiding
                    );
                    anim.setDuration(ResizeAnimation.DURATION_SHORT);
                    linearLayout.clearAnimation();
                    linearLayout.startAnimation(anim);
                    fadeInThread = null;
                    triggerFadeOutThread();
                } catch (Exception ignored) {
                }
            });
        });

        fadeInThread.start();
    }

    public void triggerFadeOutThread()
    {
        if (fadeOutThread != null) {
            fadeOutThread.interrupt();
        }

        fadeOutThread = new Thread(() -> {
            SystemClock.sleep(ResizeAnimation.DURATION_LONG);
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            runOnUiThread(() -> {
                try {
                    LinearLayout linearLayout = findViewById(R.id.overlay_transparent_extensions);

                    continueHiding.value = true;
                    ResizeAnimation anim = new ResizeAnimation(
                            linearLayout,
                            1,
                            ResizeAnimation.CHANGE_HEIGHT,
                            ResizeAnimation.IS_HIDING_ANIMATION,
                            continueHiding
                    );
                    anim.setDuration(ResizeAnimation.DURATION_SHORT);
                    linearLayout.startAnimation(anim);
                    fadeOutThread = null;
                } catch (Exception ignored) {
                }
            });
        });

        fadeOutThread.start();
    }

    public void instantFadeIn()
    {
        continueHiding.value = false;
        runOnUiThread(() -> {
            try {
                LinearLayout linearLayout = findViewById(R.id.overlay_transparent_extensions);
                linearLayout.clearAnimation();
                ViewGroup.LayoutParams layoutParams = linearLayout.getLayoutParams();
                linearLayout.setVisibility(View.VISIBLE);
                layoutParams.height = Calculator.DpToPx2(48, getResources());
                fadeInThread = null;
                triggerFadeOutThread();
            } catch (Exception ignored) {
            }
        });
    }
}

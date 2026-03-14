package com.vincentengelsoftware.androidimagecompare.ui.compare;

import android.graphics.drawable.Drawable;
import android.widget.ToggleButton;

import com.vincentengelsoftware.androidimagecompare.constants.Settings;
import com.vincentengelsoftware.androidimagecompare.ui.widget.VesImageInterface;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for wiring synchronised zoom between two image views and
 * driving the toggle button that controls it.
 *
 * <p>Non-instantiable – all methods are static.</p>
 */
public final class SyncZoom {

    private SyncZoom() { /* utility class */ }

    /**
     * Registers bidirectional mirror listeners so that gestures on either image
     * are forwarded to the other whenever {@code sync} is {@code true}.
     *
     * <p>A shared {@code disabled} guard prevents the listeners from recursively
     * triggering each other.</p>
     *
     * @param imageOne first image view
     * @param imageTwo second image view
     * @param sync     shared flag controlling whether mirroring is active
     */
    public static void setLinkedTargets(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            AtomicBoolean sync
    ) {
        // The disabled flag is shared so that while one listener is forwarding
        // a touch event the other does not echo it back.
        AtomicBoolean disabled = new AtomicBoolean(false);

        imageOne.addMirrorListener(imageTwo, sync, disabled);
        imageTwo.addMirrorListener(imageOne, sync, disabled);
    }

    /**
     * Binds a {@link ToggleButton} to the sync state, updating the button icon and
     * optionally resetting both image views' zoom when sync is re-enabled.
     *
     * @param imageOne      first image view
     * @param imageTwo      second image view
     * @param toggleButton  the UI control
     * @param iconLinkedOn  icon shown when sync is active
     * @param iconLinkedOff icon shown when sync is inactive
     * @param sync          shared flag updated by this button
     */
    public static void setUpSyncZoomToggleButton(
            VesImageInterface imageOne,
            VesImageInterface imageTwo,
            ToggleButton toggleButton,
            Drawable iconLinkedOn,
            Drawable iconLinkedOff,
            AtomicBoolean sync
    ) {
        toggleButton.setChecked(sync.get());
        toggleButton.setBackground(sync.get() ? iconLinkedOn : iconLinkedOff);

        toggleButton.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                if (Settings.RESET_IMAGE_ON_LINKING) {
                    imageOne.resetZoom();
                    imageTwo.resetZoom();
                }
                toggleButton.setBackground(iconLinkedOn);
            } else {
                toggleButton.setBackground(iconLinkedOff);
            }
            sync.set(isChecked);
        });
    }
}

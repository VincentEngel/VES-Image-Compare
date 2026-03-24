package com.vincentengelsoftware.androidimagecompare.domain.model;

/**
 * Immutable value type that captures the state of a single crop seekbar.
 *
 * <p>{@code active} indicates whether this edge participates in the crop operation; {@code
 * progress} is the seekbar percentage (0–100) that controls how far the cut extends.
 *
 * <p>Four {@code CropEdge} instances (top, left, right, bottom) are bundled together into a {@link
 * CropParams} record.
 */
public record CropEdge(boolean active, int progress) {}

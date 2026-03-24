package com.vincentengelsoftware.androidimagecompare.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Thin wrapper around {@link SharedPreferences} that provides typed get/set helpers for all
 * primitive types used by the app's settings layer.
 *
 * <p>All read and write operations silently swallow exceptions so that a corrupt preference file
 * never crashes the app; callers receive the supplied default value instead.
 */
public final class KeyValueStorage {

  /**
   * Preference key that tracks whether the user has already been asked to leave a review. Kept here
   * alongside other storage keys so that all preference key strings remain in one layer.
   */
  public static final String ASKED_FOR_REVIEW = "ASKED_FOR_REVIEW";

  private final Context context;

  public KeyValueStorage(Context context) {
    this.context = context;
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
  }

  private SharedPreferences.Editor getEditor() {
    return this.getSharedPreferences().edit();
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    try {
      return this.getSharedPreferences().getBoolean(key, defaultValue);
    } catch (Exception ignored) {
    }

    return defaultValue;
  }

  public void remove(String key) {
    try {
      SharedPreferences.Editor editor = this.getEditor();
      editor.remove(key);
      editor.apply();
    } catch (Exception ignored) {
    }
  }

  public void setBoolean(String key, boolean value) {
    try {
      SharedPreferences.Editor editor = this.getEditor();
      editor.putBoolean(key, value);
      editor.apply();
    } catch (Exception ignored) {
    }
  }

  public String getString(String key, String defaultValue) {
    try {
      return this.getSharedPreferences().getString(key, defaultValue);
    } catch (Exception ignored) {
    }

    return defaultValue;
  }

  public void setString(String key, String value) {
    try {
      SharedPreferences.Editor editor = this.getEditor();
      editor.putString(key, value);
      editor.apply();
    } catch (Exception ignored) {
    }
  }

  public void setInt(String key, int value) {
    try {
      SharedPreferences.Editor editor = this.getEditor();
      editor.putInt(key, value);
      editor.apply();
    } catch (Exception ignored) {
    }
  }

  public int getInt(String key, int defaultValue) {
    try {
      return this.getSharedPreferences().getInt(key, defaultValue);
    } catch (Exception ignored) {
    }

    return defaultValue;
  }

  public float getFloat(String key, float defaultValue) {
    try {
      return this.getSharedPreferences().getFloat(key, defaultValue);
    } catch (Exception ignored) {
    }

    return defaultValue;
  }

  public void setFloat(String key, float value) {
    try {
      SharedPreferences.Editor editor = this.getEditor();
      editor.putFloat(key, value);
      editor.apply();
    } catch (Exception ignored) {
    }
  }
}

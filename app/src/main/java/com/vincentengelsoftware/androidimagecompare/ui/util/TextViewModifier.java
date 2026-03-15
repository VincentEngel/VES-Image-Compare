package com.vincentengelsoftware.androidimagecompare.ui.util;

import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class TextViewModifier {
  public static void makeLinkClickable(TextView textView) {
    textView.setMovementMethod(LinkMovementMethod.getInstance());
  }
}

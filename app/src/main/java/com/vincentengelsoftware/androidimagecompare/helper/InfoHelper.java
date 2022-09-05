package com.vincentengelsoftware.androidimagecompare.helper;

import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class InfoHelper {
    public static void makeLinkClickable(TextView textView)
    {
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}

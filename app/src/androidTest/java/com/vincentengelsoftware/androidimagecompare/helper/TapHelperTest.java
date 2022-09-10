package com.vincentengelsoftware.androidimagecompare.helper;

import android.view.View;
import android.widget.ImageView;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.vincentengelsoftware.androidimagecompare.viewClasses.ZoomImageView;

@RunWith(AndroidJUnit4.class)
public class TapHelperTest {

    @Test
    public void setOnClickListener() {
        ZoomImageView imageViewOne = new ZoomImageView(ApplicationProvider.getApplicationContext());
        ZoomImageView imageViewTwo = new ZoomImageView(ApplicationProvider.getApplicationContext());
        imageViewOne.setVisibility(View.VISIBLE);
        imageViewTwo.setVisibility(View.INVISIBLE);

        TapHelper.setOnClickListener(imageViewOne, imageViewTwo);
        TapHelper.setOnClickListener(imageViewTwo, imageViewOne);

        imageViewOne.callOnClick();

        assertEquals(View.INVISIBLE, imageViewOne.getVisibility());
        assertEquals(View.VISIBLE, imageViewTwo.getVisibility());

        imageViewTwo.callOnClick();

        assertEquals(View.VISIBLE, imageViewOne.getVisibility());
        assertEquals(View.INVISIBLE, imageViewTwo.getVisibility());
    }
}
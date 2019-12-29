package org.desperu.go4lunch.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.desperu.go4lunch.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LayoutInflater.class, Bitmap.class})
public class MarkerUtilsTest {

    @Mock Activity mockActivity;
    @Mock LayoutInflater mockLayoutInflater;
    @Mock View mockView;
    @Mock ImageView mockImageView;
    @Mock WindowManager mockWindowManager;
    @Mock Display mockDisplay;
    @Mock Bitmap mockBitmap;

    @Before
    public void before() {
        //  mock the inflater that is returned by LayoutInflater.from()
        mockStatic(LayoutInflater.class);
        when(LayoutInflater.from(mockActivity)).thenReturn(mockLayoutInflater);

        //  pass anyInt() as a resource id to care of R.layout.custom_marker_layout
        when(mockLayoutInflater.inflate(anyInt(), eq(null))).thenReturn(mockView);

        // mock findViewById with mocked image view
        when(mockView.findViewById(anyInt())).thenReturn(mockImageView);

        // mock getWindowManager and getDefaultDisplay with mocked classes
        when(mockActivity.getWindowManager()).thenReturn(mockWindowManager);
        when(mockActivity.getWindowManager().getDefaultDisplay()).thenReturn(mockDisplay);

        // mock the bitmap that is returned by Bitmap.createBitmap()
        mockStatic(Bitmap.class);
        when(Bitmap.createBitmap(anyInt(), anyInt(), eq(Bitmap.Config.ARGB_8888))).thenReturn(mockBitmap);
    }


    @Test
    public void Given_markerView_When_convertViewToBitmap_Then_checkBitmap() {
        Bitmap bitmap = MarkerUtils.createBitmapFromView(mockActivity, R.layout.custom_marker_layout,
                R.color.colorMarkerBookedFont, R.color.colorMarkerBookedCutlery);

        assertThat("Bitmap was created", bitmap != null);
    }
}
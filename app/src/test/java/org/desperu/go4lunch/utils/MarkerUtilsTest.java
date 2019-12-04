package org.desperu.go4lunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({LayoutInflater.class})
public class MarkerUtilsTest {

    @Mock Context mockContext;
    @Mock LayoutInflater mockLayoutInflater;
    @Mock View mockView;
    @Mock ViewGroup mockParent;

    @Before
    public void before() {
        //  mock the context that comes from parent ViewGroup
        when(mockParent.getContext()).thenReturn(mockContext);

        //  mock the inflater that is returned by LayoutInflater.from()
        when(LayoutInflater.from(mockContext)).thenReturn(mockLayoutInflater);

        //  pass anyInt() as a resource id to care of R.layout.fragment_news_view_holder in onCreateViewHolder()
        when(mockLayoutInflater.inflate(anyInt(), eq(mockParent), eq(false))).thenReturn(mockView);
    }


    @Test
    public void Given_markerView_When_convertViewToBitmap_Then_checkBitmap() {
        Bitmap bitmap = MarkerUtils.createCustomMarker(mockContext, R.layout.custom_marker_layout,
                R.color.colorMarkerBookedFont, R.color.colorMarkerBookedCutlery);

//        assertNotNull(bitmap);
        assertThat("Bitmap was created", bitmap != null);
    }
}

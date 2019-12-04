package org.desperu.go4lunch.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;

import org.desperu.go4lunch.R;
import org.jetbrains.annotations.NotNull;

public class MarkerUtils {

    public static Bitmap createCustomMarker(@NotNull Context context, @LayoutRes int layout,
                                            @ColorInt int fontColor, @ColorInt int cutleryColor) {

        View marker = LayoutInflater.from(context).inflate(layout, null);

        ImageView roomImage = marker.findViewById(R.id.custom_marker_layout_room);
        roomImage.setColorFilter(fontColor);

        ImageView cutleryImage = marker.findViewById(R.id.custom_marker_layout_cutlery);
        cutleryImage.setColorFilter(cutleryColor);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
    }
}

package com.cbs.fuelest;

import android.widget.ImageView;
import androidx.databinding.BindingAdapter;

public class BindingAdapters {
    @BindingAdapter("android:src")
    public static void setImageViewResource(ImageView imageView, int resource) {
        imageView.setImageResource(resource);
    }
}
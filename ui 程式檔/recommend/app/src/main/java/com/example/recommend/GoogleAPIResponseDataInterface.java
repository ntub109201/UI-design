package com.example.recommend;

import android.graphics.Bitmap;

public interface GoogleAPIResponseDataInterface {
    public abstract String getPlaceName(int position);
    public abstract Bitmap getPlacePhoto(int position);
    public abstract int getPlaceDistance(int postion);
    public abstract int getItemsCount();
    public abstract double getLng(int position);
    public abstract double getLat(int position);
}

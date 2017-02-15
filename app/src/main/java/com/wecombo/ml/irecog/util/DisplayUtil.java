package com.wecombo.ml.irecog.util;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;

public class DisplayUtil {
    public static Point getScreenResolution(Context context) {
        DisplayMetrics dMetrics = new DisplayMetrics();
        dMetrics = context.getResources().getDisplayMetrics();
        Point screenResolution = new Point (dMetrics.widthPixels,dMetrics.heightPixels);
        return  screenResolution;
    }
}

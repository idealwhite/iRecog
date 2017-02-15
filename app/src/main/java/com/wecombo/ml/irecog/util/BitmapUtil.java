package com.wecombo.ml.irecog.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.Type;

import com.wecombo.ml.irecog.ScriptC_bitmapPreprocess;

public class BitmapUtil {
    public static Bitmap getBinaryBitmap(Context context, Bitmap bitmap, int threshold) {
        //Create RenderScript
        RenderScript rs = RenderScript.create(context);
        ScriptC_bitmapPreprocess script = new ScriptC_bitmapPreprocess(rs);
        //Create in and out allocation
        Allocation inBitmapAllocation = Allocation.createFromBitmap(rs, bitmap);
        Type type = inBitmapAllocation.getType();
        Allocation greyBitmapAllocation = Allocation.createTyped(rs, type);
        Allocation binarizedBitmapAllocation = Allocation.createTyped(rs, type);
        //RenderScript
        script.forEach_greyScale(inBitmapAllocation, greyBitmapAllocation);
        script.forEach_binarize(inBitmapAllocation, binarizedBitmapAllocation);
        Bitmap binarizedBitmap = bitmap.copy(bitmap.getConfig(), true);
        binarizedBitmapAllocation.copyTo(binarizedBitmap);
        //Release res
        inBitmapAllocation.destroy();
        greyBitmapAllocation.destroy();
        binarizedBitmapAllocation.destroy();
        script.destroy();
        rs.destroy();
        return binarizedBitmap;
    }
}

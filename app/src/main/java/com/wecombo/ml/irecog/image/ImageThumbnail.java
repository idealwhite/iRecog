package com.wecombo.ml.irecog.image;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class ImageThumbnail {

    public static int reckonThumbnail(int oldWidth, int oldHeight, int newWidth, int newHeight) {
        if ((oldHeight > newHeight && oldWidth > newWidth)
                || (oldHeight <= newHeight && oldWidth > newWidth)) {
            int be = (int) (oldWidth / (float) newWidth);
            if (be <= 1)
                be = 1;
            return be;
        } else if (oldHeight > newHeight && oldWidth <= newWidth) {
            int be = (int) (oldHeight / (float) newHeight);
            if (be <= 1)
                be = 1;
            return be;
        }
        return 1;
    }

    public static Bitmap picZoom(Bitmap bmp, int width, int height) {
        int bmpWidth = bmp.getWidth();
        int bmpHeght = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale((float) width / bmpWidth, (float) height / bmpHeght);

        return Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeght, matrix, true);
    }

    public static  Bitmap picWidthZoom(Bitmap bmp, int width) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();
        float widthScale = (float) width / bmpWidth;
        Matrix matrix = new Matrix();
        matrix.postScale(widthScale, widthScale);

        return Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
    }

}

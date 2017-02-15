package com.wecombo.ml.irecog.ocr;

import android.graphics.Bitmap;

public class OcrResult {
    private Bitmap bitmap;
    private String text;

    private long timestamp;
    private long recogCostTime;
    private long imgPreProcessCostTime;

    public OcrResult(Bitmap bitmap,
                     String text,
                     long timestamp,
                     long recogCostTime) {
        this.bitmap = bitmap;
        this.text = text;
        this.timestamp = timestamp;
        this.recogCostTime = recogCostTime;
        this.imgPreProcessCostTime = 0;
    }

    public OcrResult(Bitmap bitmap,
                     String text,
                     long timestamp,
                     long recogCostTime,
                     long imgPreProcessCostTime) {
        this.bitmap = bitmap;
        this.text = text;
        this.timestamp = timestamp;
        this.recogCostTime = recogCostTime;
        this.imgPreProcessCostTime = imgPreProcessCostTime;
    }

    public OcrResult(Bitmap bitmap) {
        this.bitmap = bitmap;
        this.text = "";
        this.timestamp = System.currentTimeMillis();
        this.recogCostTime = 0;
    }

    public Bitmap getBitmap() {return bitmap;}
    public String getText() {return text;}
    public long getTimestamp() {return timestamp;}
    public long getRecogCostTime() {return recogCostTime;}
    public long getImgPreProcessCostTime() {return imgPreProcessCostTime;}
}

package com.wecombo.ml.irecog.ocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wecombo.ml.irecog.CaptureIDCardActivity;
import com.wecombo.ml.irecog.R;
import com.wecombo.ml.irecog.image.BitmapProcessing;

/**
 * Created by LisaiZhang on 2016/12/24.
 */

public class OcrIDCardAsyncTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = "OcrAsyncTask";
    private CaptureIDCardActivity activity;
    private TessTwo tessTwo;
    private Bitmap bitmap;
    private OcrResult ocrResult;
    private BitmapProcessing bitmapProcessing;

    public OcrIDCardAsyncTask(CaptureIDCardActivity activity, TessTwo tess, Bitmap picBitmap,BitmapProcessing bitmapProcessing) {
        this.activity = activity;
        this.tessTwo = tess;
        this.bitmap = picBitmap;
        this.ocrResult = null;
        this.bitmapProcessing = bitmapProcessing;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        if (isCancelled()) return false;
        Log.e(TAG, "doInBackground: " + "Thread starts.");
        String result_text;
        try {
            long start_timestamp = System.currentTimeMillis();
            //bitmap = bitmapProcessing.denoise(bitmap);     //灰度化或二值化
            result_text = tessTwo.getOcrResult(bitmap);
            if (result_text == null || result_text.equals("")) {
                return false;
            }
            long end_timestamp = System.currentTimeMillis();
            long cost_timestamp = end_timestamp - start_timestamp;
            ocrResult = new OcrResult(bitmap, result_text, end_timestamp, cost_timestamp);
            Log.e(TAG, "doInBackground: " + ocrResult.getText());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(isCancelled()) return;
        Handler handler = activity.getHandler();
        if (handler != null) {
            if (result) {
                Message message = Message.obtain(handler, R.id.ocr_decode_success, ocrResult);
                message.sendToTarget();
            } else {
                Message message = Message.obtain(handler, R.id.ocr_decode_fail, ocrResult);
                message.sendToTarget();
            }
        }
        if (tessTwo != null) {
            tessTwo.clearImage();
        }
    }
}

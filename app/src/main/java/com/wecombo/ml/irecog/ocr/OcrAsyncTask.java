package com.wecombo.ml.irecog.ocr;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.wecombo.ml.irecog.CaptureSearchActivity;
import com.wecombo.ml.irecog.R;
import com.wecombo.ml.irecog.ocr.OcrResult;
import com.wecombo.ml.irecog.util.BitmapUtil;

public class OcrAsyncTask extends AsyncTask<Void, Void, Boolean>{

    private static final String TAG = "OcrAsyncTask";
    private CaptureSearchActivity activity;
    private TessTwo tessTwo;
    private Bitmap bitmap;
    private Bitmap binaryBitmap;
    private OcrResult ocrResult;

    public OcrAsyncTask(CaptureSearchActivity activity, TessTwo tess, Bitmap picBitmap) {
        this.activity = activity;
        this.tessTwo = tess;
        this.bitmap = picBitmap;
        this.binaryBitmap = null;
        this.ocrResult = null;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        //Thread stop signal
        if (isCancelled()) return false;
        //Generate binary bitmap
        long img_start_timestamp = System.currentTimeMillis();
        binaryBitmap = BitmapUtil.getBinaryBitmap(activity, bitmap, 88);
        long img_end_timestamp = System.currentTimeMillis();
        long img_cost_timestamp = img_end_timestamp - img_start_timestamp;
        String result_text = "preprocess";
        try {
            long ocr_start_timestamp = System.currentTimeMillis();
            //result_text = tessTwo.getOcrResult(binaryBitmap);
            if (result_text == null || result_text.equals("")) {
                return false;
            }
            long ocr_end_timestamp = System.currentTimeMillis();
            long ocr_cost_timestamp = ocr_end_timestamp - ocr_start_timestamp;
            ocrResult = new OcrResult(binaryBitmap,
                    result_text,
                    ocr_end_timestamp,
                    ocr_cost_timestamp,
                    img_cost_timestamp);
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

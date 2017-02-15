package com.wecombo.ml.irecog;

import android.os.Handler;
import android.os.Message;

import com.wecombo.ml.irecog.ocr.OcrResult;

public class CaptureSearchActivityHandler extends Handler {
    private final CaptureSearchActivity activity;
    private static State state;

    private enum State {
        START,
        OCR_SUCCESS,
        OCR_FAIL
    }

    public CaptureSearchActivityHandler(CaptureSearchActivity captureSearchActivity) {
        this.activity = captureSearchActivity;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.ocr_decode_success:
                state = State.OCR_SUCCESS;
                activity.setImgPreprocessCostTime(((OcrResult) message.obj).getImgPreProcessCostTime());
                activity.setOcrCostTime(((OcrResult) message.obj).getRecogCostTime());
                activity.setContentText(((OcrResult) message.obj).getText());
                activity.setResultThumbnail(((OcrResult) message.obj).getBitmap(), 0.4);
                activity.enableResultBox();
                break;
            case R.id.ocr_decode_fail:
                break;
            default:
                break;
        }
    }
}

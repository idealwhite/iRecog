package com.wecombo.ml.irecog;

import android.os.Handler;
import android.os.Message;

import com.wecombo.ml.irecog.ocr.OcrResult;

/**
 * Created by LisaiZhang on 2016/12/24.
 */

public class CaptureIDCardActivityHandler extends Handler {
    private final CaptureIDCardActivity activity;
    private static CaptureIDCardActivityHandler.State state;

    private enum State {
        START,
        OCR_SUCCESS,
        OCR_FAIL
    }

    public CaptureIDCardActivityHandler(CaptureIDCardActivity captureIDCardActivity) {
        this.activity = captureIDCardActivity;
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.ocr_decode_success:
                state = CaptureIDCardActivityHandler.State.OCR_SUCCESS;
                activity.setNameText(((OcrResult) message.obj).getText());
                activity.enableResultBox();
                break;
            case R.id.ocr_decode_fail:
                break;
            default:
                break;
        }
    }
}

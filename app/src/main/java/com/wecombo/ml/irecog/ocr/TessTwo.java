package com.wecombo.ml.irecog.ocr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.wecombo.ml.irecog.util.FileUtil;

public class TessTwo {
    private static final String TAG = "TessTwo";
    private TessBaseAPI tessAPI;
    private boolean init_flag;

    public TessTwo() {
        String data_path = FileUtil.getStoragePath();
        tessAPI = new TessBaseAPI();
        //字库
        init_flag = tessAPI.init(data_path, "chi_sim");
        //模式选择PSM_OSD_ONLY, PSM_AUTO, PSM_SINGLE_BLOCK, PSM_SINGLE_BLOCK_VERT_TEXT, PSM_SPARSE_TEXT_OSD, 如果切割成行可以使用PSM_SINGLE_LINE,切割成词可以使用PSM_SINGLE_WORD
        tessAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT_OSD);
    }

    public TessTwo(String charset, int mode) {
        String data_path = FileUtil.getStoragePath();
        tessAPI = new TessBaseAPI();
        //Record the init result
        init_flag = tessAPI.init(data_path, charset);
        //PSM_OSD_ONLY, PSM_AUTO, PSM_SINGLE_BLOCK, PSM_SINGLE_BLOCK_VERT_TEXT, PSM_SPARSE_TEXT_OSD
        tessAPI.setPageSegMode(mode);
    }

    public String getOcrResult(Bitmap bitmap) {
        String result = "Tess Two Init fails.";
        if (init_flag) {
            tessAPI.clear();
            tessAPI.setDebug(true);
            tessAPI.setImage(bitmap);
            result = tessAPI.getUTF8Text();
        }
        return  result;
    }

    public void clearImage() {
        tessAPI.clear();
    }
}

package com.wecombo.ml.irecog;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wecombo.ml.irecog.camera.CameraManager;
import com.wecombo.ml.irecog.image.ImageThumbnail;
import com.wecombo.ml.irecog.ocr.OcrAsyncTask;
import com.wecombo.ml.irecog.ocr.TessTwo;
import com.wecombo.ml.irecog.util.DisplayUtil;
import com.wecombo.ml.irecog.view.CircularLoadingBar;

public final class CaptureSearchActivity extends Activity implements SurfaceHolder.Callback, ImageButton.OnClickListener {

    private static final String TAG = "CaptureSearchActivity";
    private CaptureSearchActivity captureSearchActivity;
    private ImageButton takePhoteBtn;
    private LinearLayout targetRect;
    private CircularLoadingBar loadingBar;
    private boolean surfaceReady;
    private SurfaceView previewSurface;
    private SurfaceHolder previewHolder;
    private LinearLayout result_wrapper_view;
    private ImageView result_thumbnail;
    private CameraManager cameraManager;
    private String captureFilePath;
    private Bitmap captureBitmap;
    private Bitmap captureThumbnailBitmap;
    private TessTwo tessTwo;
    private Handler handler;
    private OcrAsyncTask ocrAsyncTask;

    private boolean resultShow;
    private TextView imgPreprocessCostTime;
    private TextView ocrCostTime;
    private TextView ocrResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_search);
        initView();
        takePhoteBtn.setOnClickListener(this);
    }

    private void initView() {
        //Initialize ui component
        this.captureSearchActivity = this;
        this.previewSurface = (SurfaceView) findViewById(R.id.camera_surface_view);
        this.previewHolder = previewSurface.getHolder();
        this.takePhoteBtn = (ImageButton) findViewById(R.id.btn_shutter);
        this.targetRect = (LinearLayout) findViewById(R.id.targetRect);
        this.loadingBar = (CircularLoadingBar) findViewById(R.id.circular_loading_bar);
        this.result_wrapper_view = (LinearLayout) findViewById(R.id.result_view);
        this.result_thumbnail = (ImageView) findViewById(R.id.result_pic);
        //Initialize the result box
        this.imgPreprocessCostTime = (TextView)findViewById(R.id.result_img_preprocess_time);
        this.ocrCostTime = (TextView)findViewById(R.id.result_text_recog_time);
        this.ocrResultText = (TextView)findViewById(R.id.result_content);
        //Initilize cameraManager
        this.cameraManager = new CameraManager(DisplayUtil.getScreenResolution(this));
        //Initialize capture save path
        this.captureFilePath = Environment.getExternalStorageDirectory() + "/iRecog/Capture/";
        //Initialize the flag
        this.resultShow = false;
        //Initialize the tess
        this.tessTwo = new TessTwo();
        this.handler = null;
        this.ocrAsyncTask = null;

        Log.i("initView","initialization completes!");
    }

    public Handler getHandler() {return handler;}

    @Override
    public void onBackPressed() {
        if (resultShow) {
            //Restore the camera interface
            if (ocrAsyncTask != null && ocrAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
                ocrAsyncTask.cancel(true);
            }
            disableResultBox();
            disableLoadingBar();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_shutter:
                //UI Change
                disableShutterBtn();
                disableTargetRect();
                enableLoadingBar();
                cameraManager.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        //Convert data byte to original bitmap
                        captureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        captureBitmap = generateTargetBitmanp(captureBitmap);
                        //Tess two code...
                        ocrAsyncTask = new OcrAsyncTask(captureSearchActivity, tessTwo, captureBitmap);
                        ocrAsyncTask.execute();
                        //Make the preview continuous
                        camera.autoFocus(null);
                        camera.startPreview();
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    protected void  onResume() {
        super.onResume();
        Log.i("onResume","surfaceReady="+surfaceReady);
        if (surfaceReady) {
            initCamera(previewHolder);
        }else {
            previewHolder.addCallback(this);
        }
    }

    @Override
    protected void onPause() {
        cameraManager.stopPreview();
        cameraManager.closeDriver();
        if (!surfaceReady) {
            previewHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("surfaceCreated","surfaceReady="+surfaceReady);
        if (!surfaceReady) {
            surfaceReady = true;
            initCamera(previewHolder);
            //Init the targetRect
            drawTargetRect();
            Log.i("surfaceCreated","camera starts!");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * 初始camera
     *
     * @param surfaceHolder SurfaceHolder
     */
    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.e("initCamera","camera not open!");
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            handler = new CaptureSearchActivityHandler(this);
            if (cameraManager.isOpen()) {
                Log.e("initCamera","camera still not open!");
            }
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager.startPreview();
        } catch (Exception ioe) {

        }
    }

    private void drawTargetRect() {
        int btnWidth = takePhoteBtn.getWidth();
        int btnMarginEnd = 10;
        int surfaceWidth = previewSurface.getWidth();
        int rectWidth = targetRect.getWidth();
        int rectMarginLeft = (surfaceWidth - rectWidth - btnWidth - btnMarginEnd) / 2;
        RelativeLayout.LayoutParams rectParams = (RelativeLayout.LayoutParams) targetRect.getLayoutParams();
        rectParams.leftMargin = rectMarginLeft;
        Log.e(TAG, "drawTargetRect: " + rectMarginLeft);
        targetRect.setLayoutParams(rectParams);
    }

    private Bitmap generateTargetBitmanp(Bitmap bitmap) {
        //Acquire targetRect position on screen
        int[] location = new int[2];
        targetRect.getLocationOnScreen(location);
        //Cut off the part we need
        int rectWidth = targetRect.getWidth();
        int rectHeight = targetRect.getHeight();
        Bitmap rectBitmap = Bitmap.createBitmap(bitmap, location[0], location[1], rectWidth, rectHeight);
        return rectBitmap;
    }

    public void setImgPreprocessCostTime(long costTime) { imgPreprocessCostTime.setText(Long.toString(costTime));}

    public void setOcrCostTime(long costTime) { ocrCostTime.setText(Long.toString(costTime));}

    public void setContentText(String content) {
        ocrResultText.setText(content);
    }

    public void setResultThumbnail(Bitmap bitmap, double scale) {
        //Generate and display thumbnail
        int thumbnail_width = (int) (cameraManager.getCameraInstance()
                .getParameters().getPictureSize().width * scale);
        captureThumbnailBitmap = ImageThumbnail.picWidthZoom(bitmap, thumbnail_width);
        result_thumbnail.setImageBitmap(captureThumbnailBitmap);
    }

    public void enableLoadingBar() {
        resultShow = true;
        loadingBar.setVisibility(View.VISIBLE);
    }

    public void disableLoadingBar() {
        loadingBar.setVisibility(View.GONE);
    }

    public void enableShutterBtn() {
        takePhoteBtn.setEnabled(true);
        takePhoteBtn.setVisibility(View.VISIBLE);
    }

    public void disableShutterBtn() {
        takePhoteBtn.setEnabled(false);
        takePhoteBtn.setVisibility(View.GONE);
    }

    public void enableTargetRect() {
        targetRect.setVisibility(View.VISIBLE);
    }

    public void disableTargetRect() {
        targetRect.setVisibility(View.GONE);
    }

    public void enableResultBox() {
        resultShow = true;
        disableLoadingBar();
        result_wrapper_view.setVisibility(View.VISIBLE);
    }

    private void disableResultBox() {
        result_wrapper_view.setVisibility(View.GONE);
        enableShutterBtn();
        enableTargetRect();
        resultShow = false;
    }

    public void sendMessage(int number,String message)
    {
        Message msg = handler.obtainMessage();
        msg.what = number;
        msg.obj = message ;
        this.handler.sendMessage(msg);
    }
}


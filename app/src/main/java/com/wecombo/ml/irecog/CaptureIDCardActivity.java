package com.wecombo.ml.irecog;

import com.wecombo.ml.irecog.camera.CameraManager;
import com.wecombo.ml.irecog.image.BitmapProcessing;
import com.wecombo.ml.irecog.image.ImageThumbnail;
import com.wecombo.ml.irecog.ocr.OcrAsyncTask;
import com.wecombo.ml.irecog.ocr.OcrIDCardAsyncTask;
import com.wecombo.ml.irecog.ocr.OcrResult;
import com.wecombo.ml.irecog.ocr.TessTwo;
import com.wecombo.ml.irecog.util.NLPUtil;
import com.wecombo.ml.irecog.view.CircularLoadingBar;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.in;

public class CaptureIDCardActivity extends Activity implements SurfaceHolder.Callback, ImageButton.OnClickListener {

    private ImageButton takePhoteBtn;
    private CircularLoadingBar loadingBar;
    private LinearLayout targetRect;
    private boolean surfaceReady;
    private SurfaceView previewSurface;
    private SurfaceHolder previewHolder;
    private LinearLayout result_view;
    private ImageView result_thumbnail;
    private CameraManager cameraManager;
    private String captureFilePath;
    private Bitmap captureBitmap;
    private Bitmap captureThumbnailBitmap;
    private TessTwo tessTwo;
    private Handler handler;
    private OcrIDCardAsyncTask ocrAsyncTask;
    private CaptureIDCardActivity captureIDCardActivity;
    private OcrResult ocrResult;
    private BitmapProcessing bitmapProcessing;

    private boolean resultShow;
    private TextView result_name;
    private TextView result_gender;
    private TextView result_ethnic;
    private TextView result_birth;
    private TextView result_addr;
    private TextView result_id;

    private ImageView result_pre1;
    private ImageView result_pre2;
    private ImageView result_pre3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_idcard);
        initView();
        takePhoteBtn.setOnClickListener(this);
    }

    private void initView() {
        //Initialize ui component
        this.captureIDCardActivity = this;
        this.previewSurface = (SurfaceView) findViewById(R.id.camera_surface_view);
        this.previewHolder = previewSurface.getHolder();
        this.takePhoteBtn = (ImageButton) findViewById(R.id.btn_shutter);
        this.targetRect = (LinearLayout) findViewById(R.id.targetRect);
        this.loadingBar = (CircularLoadingBar) findViewById(R.id.circular_loading_bar);
        this.result_view = (LinearLayout) findViewById(R.id.result_view);
        this.result_thumbnail = (ImageView) findViewById(R.id.result_pic);

        this.result_pre1 = (ImageView)findViewById(R.id.result_preview1);
        this.result_pre2 = (ImageView)findViewById(R.id.result_preview2);
        this.result_pre3 = (ImageView)findViewById(R.id.result_preview3);
        //Initialize the result box
        this.result_addr = (TextView) findViewById(R.id.result_addr);
        this.result_name = (TextView) findViewById(R.id.result_name);
        this.result_gender = (TextView) findViewById(R.id.result_gender);
        this.result_ethnic = (TextView) findViewById(R.id.result_ethnic);
        this.result_birth = (TextView) findViewById(R.id.result_birth);
        this.result_id = (TextView) findViewById(R.id.result_id);
        //Initialize cameraManager
        this.cameraManager = new CameraManager(getScreenResolution());
        //Initialize capture save path
        this.captureFilePath = Environment.getExternalStorageDirectory() + "/iRecog/Capture/";
        //Initialize the flag
        this.resultShow = false;
        //Initialize the tess
        this.tessTwo = new TessTwo();
        this.ocrResult = null;
        this.handler = null;
        this.ocrAsyncTask = null;
        //Initialize the OpenCV
        this.bitmapProcessing = new BitmapProcessing(this.captureIDCardActivity);
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
            enableShutterBtn();
            disableLoadingBar();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_shutter:
                takePhoteBtn.setEnabled(false);
                cameraManager.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                    //Convert data byte to original bitmap
                    captureBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    captureBitmap = generateTargetBitmanp(captureBitmap);
                    //Generate and display thumbnail
                    int thumbnail_width = (int) (cameraManager.getCameraInstance()
                            .getParameters().getPictureSize().width * 0.3);
                    captureThumbnailBitmap = ImageThumbnail.picWidthZoom(captureBitmap, thumbnail_width);
                    //result_thumbnail.setImageBitmap(bitmapProcessing.denoise(captureThumbnailBitmap));
                    result_thumbnail.setImageBitmap(captureThumbnailBitmap);
                    //Tess two code...
                    disableShutterBtn();
                    enableLoadingBar();
                    ocrAsyncTask = new OcrIDCardAsyncTask(captureIDCardActivity, tessTwo, captureBitmap, bitmapProcessing);
                    ocrAsyncTask.execute();
                    //Make the preview continuous
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
        drawTargetRect();
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
            drawTargetRect();
            Log.i("surfaceCreated","camera starts!");
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceReady = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
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
            handler = new CaptureIDCardActivityHandler(this);
            if (cameraManager.isOpen()) {
                Log.e("initCamera","camera still not open!");
            }
            // 创建一个handler来打开预览，并抛出一个运行时异常
            cameraManager.startPreview();
        } catch (Exception ioe) {

        }
    }

    private Point getScreenResolution() {
        DisplayMetrics dMetrics = new DisplayMetrics();
        dMetrics = this.getResources().getDisplayMetrics();
        Point screenResolution = new Point (dMetrics.widthPixels,dMetrics.heightPixels);
        return  screenResolution;
    }

    /**
     * 显示识别结果
     * @param content
     */
    public void setNameText(String content) {
        this.result_addr.setText(NLPUtil.getIDCardInfo(content));
        this.result_name.setText("");
        this.result_gender.setText("");
        this.result_ethnic.setText("");
        this.result_birth.setText("");
        this.result_id.setText("");
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

    public void enableResultBox() {
        resultShow = true;
        disableLoadingBar();
        result_view.setVisibility(View.VISIBLE);
    }

    private void disableResultBox() {
        result_view.setVisibility(View.GONE);
        enableShutterBtn();
        resultShow = false;
    }

    public void sendMessage(int number,String message)
    {
        Message msg = handler.obtainMessage();
        msg.what = number;
        msg.obj = message ;
        this.handler.sendMessage(msg);
    }

    private void drawTargetRect() {
        int btnWidth = takePhoteBtn.getWidth();
        int btnMarginEnd = 10;
        int surfaceWidth = previewSurface.getWidth();
        int rectWidth = targetRect.getWidth();
        int rectMarginLeft = (surfaceWidth - rectWidth - btnWidth - btnMarginEnd) / 2;
        RelativeLayout.LayoutParams rectParams = (RelativeLayout.LayoutParams) targetRect.getLayoutParams();
        rectParams.leftMargin = rectMarginLeft;
        targetRect.setLayoutParams(rectParams);
    }
}

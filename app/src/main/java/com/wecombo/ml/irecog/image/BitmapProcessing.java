package com.wecombo.ml.irecog.image;

import android.graphics.Bitmap;
import android.util.Log;

import com.wecombo.ml.irecog.CaptureIDCardActivity;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC3;

/**
 *
 * Created by LisaiZhang on 2016/12/25.
 */

public class BitmapProcessing {
    private String TAG = "BitmapProcessing";

    /**
     * Auto callback of OpenCV.
     */
    private BaseLoaderCallback mLoaderCallback;

    public BitmapProcessing(CaptureIDCardActivity activity){
        this.mLoaderCallback = new BaseLoaderCallback(activity) {
            @Override
            public void onManagerConnected(int status) {
                // TODO Auto-generated method stub
                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        Log.i(TAG, "成功加载");
                        break;
                    default:
                        super.onManagerConnected(status);
                        Log.i(TAG, "加载失败");
                        break;
                }

            }
        };
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_4, activity.getApplicationContext(), mLoaderCallback);
    }


    /**
     * 将此Bitmap执行灰度化cvtColor或二值化threshold后生成新的Bitmap并返回，颜色格式为ARGB_8888
     * @param srcBitmap
     * @return
     */
    public List<Bitmap> cutBitmap(Bitmap srcBitmap){
        Mat rgbMat = new Mat();
        Mat destMat = new Mat();
        List<MatOfPoint> matList = new ArrayList<>();
        List<Bitmap> bitmaps = new ArrayList<>();
        //Image processing
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        Imgproc.cvtColor(rgbMat, destMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Imgproc.threshold(destMat,destMat,0,255,Imgproc.THRESH_BINARY_INV|Imgproc.THRESH_OTSU);    //此为二值化操作，仅仅设置阈值，小于阈值为黑，大于为白
        Imgproc.erode(destMat,destMat,new Mat(4,4, CvType.CV_8U), new Point(-1,-1),2);//腐蚀两次 Mat单元大小，这里是5*5的8位单元 Point腐蚀位置，为负值取核中心
        Imgproc.dilate(destMat,destMat,new Mat(3,3, CvType.CV_8U), new Point(-1,-1),1);//膨胀两次 Mat单元大小，这里是5*5的8位单元 Point膨胀位置，为负值取核中心
        Utils.matToBitmap(destMat, destBitmap);
        bitmaps.add(destBitmap);
        Imgproc.findContours(destMat,matList,destMat.clone(),Imgproc.RETR_EXTERNAL,Imgproc.CHAIN_APPROX_SIMPLE);
        for(int i = 0;i < matList.size();i++){
            //TODO: cut the black area.
            Rect rc = Imgproc.boundingRect(matList.get(i));
            Bitmap newMap = Bitmap.createBitmap(srcBitmap,rc.x,rc.y,rc.width,rc.height);
            //Utils.matToBitmap(new Mat(rgbMat,rc), newMap); //convert mat to bitmap
            bitmaps.add(newMap);
        }


        Log.i(TAG, "cutBitmap sucess...");
        return bitmaps;
    }

    /**
     * 将此Bitmap执行灰度化cvtColor或二值化threshold后生成新的Bitmap并返回，颜色格式为ARGB_8888
     * @param srcBitmap
     * @return
     */
    public Bitmap denoise(Bitmap srcBitmap){
        Mat rgbMat = new Mat();
        Mat destMat = new Mat();
        //Image processing
        Bitmap destBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(srcBitmap, rgbMat);//convert original bitmap to Mat, R G B.
        //Imgproc.cvtColor(rgbMat, destMat, Imgproc.COLOR_RGB2GRAY);//rgbMat to gray grayMat
        Imgproc.GaussianBlur(rgbMat,rgbMat,new Size(1,1),0,0);
        //Imgproc.blur(rgbMat,rgbMat,new Size(1,1),new Point());
        //Imgproc.medianBlur(rgbMat,rgbMat,1);
        //Imgproc.threshold(rgbMat,rgbMat,105,255,Imgproc.THRESH_BINARY);    //此为二值化操作，仅仅设置阈值，小于阈值为黑，大于为白

        Utils.matToBitmap(rgbMat, destBitmap);
        Log.i(TAG, "cutBitmap sucess...");
        return destBitmap;
    }
}

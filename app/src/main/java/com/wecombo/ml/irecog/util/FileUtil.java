package com.wecombo.ml.irecog.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

public class FileUtil {
    private static final String APP_NAME = "iRecog";
    private static final File PARENT_PATH = Environment.getExternalStorageDirectory();
    private static String STORAGE_PATH = "";

    public static String getStoragePath(){
        if(STORAGE_PATH.equals("")){
            STORAGE_PATH = PARENT_PATH.getAbsolutePath()+ "/" + APP_NAME + "/";
            File f = new File(STORAGE_PATH);
            if(!f.exists()){
                f.mkdir();
            }
        }
        return STORAGE_PATH;
    }

    public static boolean copyFromAsset(Context context, String old_path, String new_path) {
        try {
            String file_names[] = context.getAssets().list(old_path);
            if (file_names.length > 0) {
                //If directory
                new_path = getStoragePath() + new_path;
                File file = new File(new_path);
                file.mkdirs();
                for (String file_name : file_names) {
                    //Recursion
                    copyFromAsset(context, old_path + "/" + file_name, new_path + "/" + file_name);
                }
            } else {
                //If file
                InputStream is = context.getAssets().open(old_path);
                FileOutputStream fos = new FileOutputStream(new File(new_path));
                byte[] buffer = new byte[1024];
                int byte_count = 0;
                //Read from InputStream and write into OutputStream by one buffer at one time
                while ((byte_count = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byte_count);
                }
                //Refresh buffer
                fos.flush();
                is.close();
                fos.close();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}

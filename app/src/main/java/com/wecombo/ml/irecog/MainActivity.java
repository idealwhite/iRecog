package com.wecombo.ml.irecog;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wecombo.ml.irecog.util.FileUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final File TESS_DATA = new File(Environment.getExternalStorageDirectory() + "/tessdata");
    private boolean init_flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // #Control the drawable icon size
        // Activity Dispatch
        ((Button)findViewById(R.id.ExtractIdCard))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View v) {
                        Intent intent = new Intent(MainActivity.this, CaptureIDCardActivity.class);
                        MainActivity.this.startActivityForResult(intent, 100);
                    }
                });
        ((Button)findViewById(R.id.RealTimeRecog))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick (View v) {
                        Intent intent = new Intent(MainActivity.this, CaptureSearchActivity.class);
                        MainActivity.this.startActivityForResult(intent, 100);
                    }
                });
        init_flag = checkResources();
    }

    private boolean checkResources() {
        boolean check_flag = false;
        if (!TESS_DATA.exists()) {
            check_flag = FileUtil.copyFromAsset(this, "tessdata", "tessdata");
        } else {
            check_flag = true;
        }
        return check_flag;
    }
}

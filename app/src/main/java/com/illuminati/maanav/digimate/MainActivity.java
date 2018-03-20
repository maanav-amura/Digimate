package com.illuminati.maanav.digimate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    public static String TITLE;
    public static int PAGES;
    public static int CURRENT_PAGE;
    private int TIME_OUT = 1000;
    private String TAG = "MainActivity";
    private EditText etTitle, etPages;
    private Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestApplicationPermission();

        initActivity();

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TITLE = etTitle.getText().toString();
                String pages = etPages.getText().toString();
                if (TITLE.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter PDF title",
                            Toast.LENGTH_SHORT).show();
                } else if (pages.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter PDF pages",
                            Toast.LENGTH_SHORT).show();
                } else {
                    PAGES = Integer.parseInt(pages);
                    CURRENT_PAGE = 0;
                    if (CURRENT_PAGE < PAGES) {
                        startCamera();
                    }
                }
            }
        });
    }

    private void initActivity() {
        etTitle = findViewById(R.id.editTitle);
        etPages = findViewById(R.id.editPages);
        buttonStart = findViewById(R.id.buttonStart);
    }

    private void requestApplicationPermission() {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
        }

        // Check for the external storage permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    private void startCamera() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(MainActivity.this, CaptureActivity.class);
                startActivity(i);
            }
        }, TIME_OUT);
    }
}

package com.illuminati.maanav.digimate;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.googlecode.leptonica.android.GrayQuant;
import com.googlecode.leptonica.android.Pix;


public class BinarizationActivity extends AppCompatActivity implements View.OnClickListener, AppCompatSeekBar.OnSeekBarChangeListener {
    public static Bitmap umbralization;
    public static int language;
    private ImageView img;
    private Toolbar toolbar;
    private AppCompatSeekBar seekBar;
    private Pix pix;
    private FloatingActionButton fab;
    private Spinner spinner;
    private int TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binarization);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        ViewCompat.setElevation(toolbar,10);
//        ViewCompat.setElevation((LinearLayout) findViewById(R.id.extension),10);
//        spinner = (Spinner) findViewById(R.id.language);

        img = findViewById(R.id.croppedImage);
        fab = findViewById(R.id.nextStep);
        fab.setOnClickListener(this);
        pix = com.googlecode.leptonica.android.ReadFile.readBitmap(CaptureActivity.capturedImage);

//        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, Arrays.asList("English", "Spanish"));
//        spinner.setAdapter(adapter);
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                ((TextView) view).setTextColor(Color.WHITE);
//                language = i;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        OtsuThresholder otsuThresholder = new OtsuThresholder();
        int threshold = otsuThresholder.doThreshold(pix.getData());
        /* increase threshold because is better*/
        threshold += 20;
        umbralization = com.googlecode.leptonica.android.WriteFile.writeBitmap(GrayQuant.pixThresholdToBinary(pix, threshold));
        img.setImageBitmap(umbralization);
        seekBar = findViewById(R.id.umbralization);
        seekBar.setProgress(Integer.valueOf((50 * threshold) / 254));
        seekBar.setOnSeekBarChangeListener(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.performClick();
                finish();
            }
        }, TIME_OUT);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        umbralization = com.googlecode.leptonica.android.WriteFile.writeBitmap(
                GrayQuant.pixThresholdToBinary(pix, Integer.valueOf(((254 * seekBar.getProgress()) / 50)))
        );
        img.setImageBitmap(umbralization);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.nextStep) {
            Intent intent = new Intent(BinarizationActivity.this, RecognizerActivity.class);
            startActivity(intent);
        }
    }
}

package com.illuminati.maanav.digimate;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecognizerActivity extends AppCompatActivity implements Toolbar.OnMenuItemClickListener {
    private static final String DATA_PATH = Environment.getExternalStorageDirectory() + File.separator + "abc";
    ProgressDialog progressCopy, progressOcr;
    TessBaseAPI baseApi;
    AsyncTask<Void, Void, Void> copy = new copyTask();
    AsyncTask<Void, Void, Void> ocr = new ocrTask();
    private Toolbar toolbar;
    private EditText search;
    private TextView textView;
    private String textScanned;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizer);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setOnMenuItemClickListener(this);
//        ViewCompat.setElevation(toolbar,10);
//        ViewCompat.setElevation((LinearLayout) findViewById(R.id.extension),10);
        textView = (TextView) findViewById(R.id.textExtracted);
        textView.setMovementMethod(new ScrollingMovementMethod());
        search = (EditText) findViewById(R.id.search_text);
        // Setting progress dialog for copy job.
        progressCopy = new ProgressDialog(RecognizerActivity.this);
        progressCopy.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressCopy.setIndeterminate(true);
        progressCopy.setCancelable(false);
        progressCopy.setTitle("Dictionaries");
        progressCopy.setMessage("Copying dictionary files");
        // Setting progress dialog for ocr job.
        progressOcr = new ProgressDialog(this);
        progressOcr.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressOcr.setIndeterminate(true);
        progressOcr.setCancelable(false);
        progressOcr.setTitle("OCR");
        progressOcr.setMessage("Extracting text, please wait");
        textScanned = "";

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String ett = search.getText().toString().replaceAll("\n", " ");
                String tvt = textView.getText().toString().replaceAll("\n", " ");
                textView.setText(textView.getText().toString());
                if (!ett.toString().isEmpty()) {
                    int ofe = tvt.toLowerCase().indexOf(ett.toLowerCase(), 0);
                    Spannable WordtoSpan = new SpannableString(textView.getText());
                    for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                        ofe = tvt.toLowerCase().indexOf(ett.toLowerCase(), ofs);
                        if (ofe == -1)
                            break;
                        else {
                            WordtoSpan.setSpan(new BackgroundColorSpan(ContextCompat.getColor(RecognizerActivity.this, R.color.colorAccent)), ofe, ofe + ett.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textView.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
                        }

                    }
                }
            }
        });

        copy.execute();
        ocr.execute();


        Document document = new Document();
        String dirpath = android.os.Environment.getExternalStorageDirectory().toString();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(dirpath + "/example.pdf")); //  Change pdf's name.
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        document.open();
        Image img = null;  // Change image's name and extension.
        try {
            img = Image.getInstance(dirpath + "/abc/" + "temp1.jpg");
            Log.w("sdfasdfasdf", img.toString().length() + "");
        } catch (BadElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                - document.rightMargin() - 0) / img.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
        img.scalePercent(scaler);
        img.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
        //img.setAlignment(Image.LEFT| Image.TEXTWRAP);

 /* float width = document.getPageSize().width() - document.leftMargin() - document.rightMargin();
 float height = document.getPageSize().height() - document.topMargin() - document.bottomMargin();
 img.scaleToFit(width, height)*/  // Or try this.

        try {
            document.add(img);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        document.close();


    }

    private void recognizeText() {
        String language = "";
        if (BinarizationActivity.language == 0)
            language = "eng";
        else
            language = "spa";

        baseApi = new TessBaseAPI();
        baseApi.init(DATA_PATH, language, TessBaseAPI.OEM_TESSERACT_ONLY);
//        baseApi.init("/mnt/sdcard/tesseract", "eng");
        baseApi.setImage(BinarizationActivity.umbralization);
        textScanned = baseApi.getUTF8Text();

    }


    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("trainneddata");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for (String filename : files) {
            Log.i("files", filename);
            InputStream in = null;
            OutputStream out = null;
            String dirout = DATA_PATH + "tessdata/";
            File outFile = new File(dirout, filename);
            if (!outFile.exists()) {
                try {
                    in = assetManager.open("trainneddata/" + filename);
                    (new File(dirout)).mkdirs();
                    out = new FileOutputStream(outFile);
                    copyFile(in, out);
                    in.close();
                    in = null;
                    out.flush();
                    out.close();
                    out = null;
                } catch (IOException e) {
                    Log.e("tag", "Error creating files", e);
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy_text:
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("TextScanner", textView.getText());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(RecognizerActivity.this, "Text has been copied to clipboard", Toast.LENGTH_LONG).show();
                break;
            case R.id.new_scan:
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
        }
        return false;
    }

    private class copyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCopy.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressCopy.cancel();
            progressOcr.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i("CopyTask", "copying..");
            copyAssets();
            return null;
        }
    }

    private class ocrTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressOcr.cancel();
            textView.setText(textScanned);

        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i("OCRTask", "extracting..");
            recognizeText();
            return null;
        }
    }
}

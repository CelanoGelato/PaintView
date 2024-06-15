package com.celano.base.ardrawingsketchandpaint;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ortiz.touchview.TouchImageView;
import com.otaliastudios.cameraview.CameraView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageThresholdEdgeDetection;


public class CameraActivity extends AppCompatActivity {
    String[] PERMISSIONS;
    ExecutorService executorService;

    TouchImageView objImage;
    SeekBar alphaSeek;
    TextView opacityText;
    CameraView cameraView;

    ImageView ivConvertBitmap;

    ProgressDialog ringProgressDialog;

    boolean is_edit_sketch = false;


    Bitmap bmOriginal;

    Bitmap convertedBitmap = null;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);
        setContentView(R.layout.activity_camera);
        this.executorService = Executors.newSingleThreadExecutor();
        if (Build.VERSION.SDK_INT >= 33) {
            this.PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.READ_MEDIA_IMAGES"};
        } else {
            this.PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"};
        }

        this.objImage = findViewById(R.id.objImage);
        this.alphaSeek = findViewById(R.id.alpha_seek);
        this.opacityText = findViewById(R.id.opacity_text);
        this.cameraView = findViewById(R.id.camera_view);
        this.ivConvertBitmap = findViewById(R.id.iv_convert_bitmap);

        this.cameraView.open();
        this.cameraView.clearFocus();

        this.objImage.setEnabled(true);
        bmOriginal = BitmapFactory.decodeResource(getResources(), R.raw.image);
        this.objImage.setImageBitmap(bmOriginal);
        this.objImage.setAlpha(0.4f);
        this.alphaSeek.setProgress(4);
        this.opacityText.setText("" + (this.alphaSeek.getProgress() * 10) + " %");

        this.alphaSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
                CameraActivity.this.objImage.setAlpha(((float) i) / 10.0f);
                CameraActivity.this.opacityText.setText("" + (i * 10) + " %");
            }
        });

        this.ivConvertBitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConvertBorderBitmap();

            }
        });
    }

    private void ConvertBorderBitmap() {
        final GPUImage gPUImage = new GPUImage(this);
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.ringProgressDialog = progressDialog;
        progressDialog.setMessage("Convert Bitmap");
        this.ringProgressDialog.setCancelable(false);
        runOnUiThread(new Runnable() {

            public final void run() {
                CameraActivity.this.ringProgressDialog.show();
            }
        });
        this.executorService.execute(new Runnable() {

            public void run() {
                try {
                    if (!CameraActivity.this.is_edit_sketch) {
                        gPUImage.setImage(CameraActivity.this.bmOriginal);
                        gPUImage.setFilter(new GPUImageThresholdEdgeDetection());
                        Bitmap bitmapWithFilterApplied = gPUImage.getBitmapWithFilterApplied();
                        if (bitmapWithFilterApplied != null) {
                            CameraActivity.this.convertedBitmap = getBitmapWithTransparentBG(bitmapWithFilterApplied, -1);
                        } else {
                            runOnUiThread(new Runnable() {

                                public final void run() {
                                    Toast.makeText(CameraActivity.this, "Can't Convert this image try with another", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {

                    public void run() {
                        CameraActivity.this.ringProgressDialog.dismiss();
                    }
                });

            }
        });
        this.ringProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            public void onDismiss(DialogInterface dialogInterface) {
                if (!CameraActivity.this.is_edit_sketch) {
                    if (CameraActivity.this.convertedBitmap != null) {
                        CameraActivity.this.is_edit_sketch = true;
                        CameraActivity.this.objImage.setImageBitmap(CameraActivity.this.convertedBitmap);
                        CameraActivity.this.ivConvertBitmap.setImageResource(R.drawable.sketch_on);
                        return;
                    }
                    Toast.makeText(CameraActivity.this, "Can't Convert this image try with another", Toast.LENGTH_SHORT).show();
                } else if (CameraActivity.this.bmOriginal != null) {
                    CameraActivity.this.is_edit_sketch = false;
                    CameraActivity.this.objImage.setImageBitmap(CameraActivity.this.bmOriginal);
                    CameraActivity.this.ivConvertBitmap.setImageResource(R.drawable.sketch_off);
                } else {
                    Toast.makeText(CameraActivity.this, "Can't Convert this image try with another", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public static Bitmap getBitmapWithTransparentBG(Bitmap bitmap, int i) {
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        int width = copy.getWidth();
        int height = copy.getHeight();
        for (int i2 = 0; i2 < height; i2++) {
            for (int i3 = 0; i3 < width; i3++) {
                if (copy.getPixel(i3, i2) == i) {
                    copy.setPixel(i3, i2, 0);
                }
            }
        }
        return copy;
    }

}
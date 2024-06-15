package com.celano.base.ardrawingsketchandpaint;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnPaintActivity).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PaintActivity.class)));
        findViewById(R.id.btnDrawingActivity).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DrawingActivity.class)));

        findViewById(R.id.btnCameraActivity).setOnClickListener(v -> {
//                startActivity(new Intent(MainActivity.this, DrawingActivity.class));
        });
    }
}